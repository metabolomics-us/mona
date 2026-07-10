package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service

import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{DeletionJob, UploadJob}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{DeletionJobRepository, UploadJobRepository}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.{Propagation, Transactional}

import scala.jdk.CollectionConverters._

/**
  * Marks chunked uploads abandoned by a closed tab or refresh. A job sitting at UPLOADING with no
  * chunk activity for longer than the threshold is flagged INTERRUPTED so the status page can show
  * it and offer a resume. The next chunk PUT against the job (see UploadSchedulerController) revives
  * it back to UPLOADING, so this sweep never needs to reason about resumes itself
  */
@Service
class UploadJobSweepService extends LazyLogging {

  @Autowired
  val uploadJobRepository: UploadJobRepository = null

  @Autowired
  val deletionJobRepository: DeletionJobRepository = null

  @Autowired
  val uploadStorageService: UploadStorageService = null

  @Value("${mona.uploads.interrupted-threshold-minutes:5}")
  val thresholdMinutes: Int = 5

  private val sweepInProgress: AtomicBoolean = new AtomicBoolean(false)
  private val finalizeInProgress: AtomicBoolean = new AtomicBoolean(false)

  @Scheduled(cron = "0 * * * * *")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  def sweepStaleUploads(): Unit = {
    if (!sweepInProgress.compareAndSet(false, true)) {
      logger.info("Upload sweep already in progress, skipping this run")
    } else {
      try {
        val threshold = new Date(System.currentTimeMillis() - thresholdMinutes * 60 * 1000L)

        uploadJobRepository.findByStatusAndLastUpdatedBefore(UploadJob.STATUS_UPLOADING, threshold).asScala.foreach { job =>
          job.setStatus(UploadJob.STATUS_INTERRUPTED)
          uploadJobRepository.save(job)
          logger.info(s"marked upload job ${job.getId} INTERRUPTED (idle since ${job.getLastUpdated})")
        }
      } finally {
        sweepInProgress.set(false)
      }
    }
  }

  /**
    * A job still sitting at INTERRUPTED by the time this weekly pass runs was never resumed, so
    * it's treated as permanently abandoned: its orphaned file is removed and the job is converted
    * to FAILED with a message telling the user it can no longer be resumed or revived, only
    * deleted and retried. The DB row is kept (only the file is removed) so it still shows in the
    * user's upload history. The same pass also reconciles any job still sitting at DELETING, a
    * backstop for SpectrumDeletionListener.closeUploadJobLoop missing its one shot at closing the
    * loop (e.g. an exception there, or the message getting lost)
    */
  @Scheduled(cron = "0 0 0 ? * SAT")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  def finalizeAbandonedUploads(): Unit = {
    if (!finalizeInProgress.compareAndSet(false, true)) {
      logger.info("Abandoned upload finalization already in progress, skipping this run")
    } else {
      try {
        var count = 0
        uploadJobRepository.findByStatus(UploadJob.STATUS_INTERRUPTED).asScala.foreach { job =>
          uploadStorageService.deleteJob(job.getId)
          job.setStatus(UploadJob.STATUS_FAILED)
          job.setErrorMessage("Abandoned, delete and retry")
          job.setLastUpdated(new Date())
          uploadJobRepository.save(job)
          count += 1
          logger.info(s"finalized abandoned upload job ${job.getId} as FAILED")
        }
        logger.info(s"abandoned upload finalization complete: $count job(s) finalized")

        reconcileStuckCancellations()
        reconcileStuckDeletions()
      } finally {
        finalizeInProgress.set(false)
      }
    }
  }

  /**
    * A job still sitting at CANCELLING by the time this weekly pass runs was never finalized by
    * UploadJobListener, either because the service died before honoring the request or because
    * the cancel raced the listener's own terminal save. The requested outcome already happened
    * (nothing is parsing anymore), so this just closes the loop: CANCELLED, file removed
    */
  private def reconcileStuckCancellations(): Unit = {
    var count = 0
    uploadJobRepository.findByStatus(UploadJob.STATUS_CANCELLING).asScala.foreach { job =>
      uploadStorageService.deleteJob(job.getId)
      job.setStatus(UploadJob.STATUS_CANCELLED)
      job.setLastUpdated(new Date())
      uploadJobRepository.save(job)
      count += 1
      logger.info(s"reconciled upload job ${job.getId} as CANCELLED (cancel request was never finalized)")
    }
    if (count > 0) {
      logger.info(s"stuck cancellation reconciliation complete: $count job(s) reconciled")
    }
  }

  /**
    * A job still sitting at DELETING by the time this weekly pass runs missed having its loop
    * closed. Its linked DeletionJob is the source of truth: if that actually reached COMPLETE, the
    * spectra are gone, so this closes the loop retroactively (DELETED + deletedDate) exactly as
    * SpectrumDeletionListener would have. Otherwise (the DeletionJob is FAILED, missing, or never
    * got past SCHEDULED/RUNNING in a whole week) something never finished, so the job is marked
    * FAILED rather than left showing DELETING forever
    */
  private def reconcileStuckDeletions(): Unit = {
    var count = 0
    uploadJobRepository.findByStatus(UploadJob.STATUS_DELETING).asScala.foreach { job =>
      val deletionJob: DeletionJob = deletionJobRepository.findByUploadJobId(job.getId)

      if (deletionJob != null && deletionJob.getStatus == DeletionJob.STATUS_COMPLETE) {
        job.setStatus(UploadJob.STATUS_DELETED)
        job.setDeletedDate(new Date())
        uploadJobRepository.save(job)
        logger.info(s"reconciled upload job ${job.getId} as DELETED (deletion job ${deletionJob.getId} was already complete)")
      } else {
        job.setStatus(UploadJob.STATUS_FAILED)
        job.setErrorMessage("Deletion of upload failed")
        job.setLastUpdated(new Date())
        uploadJobRepository.save(job)
        logger.warn(s"marked upload job ${job.getId} FAILED, stuck in DELETING with deletion job status ${Option(deletionJob).map(_.getStatus).getOrElse("missing")}")
      }
      count += 1
    }
    if (count > 0) {
      logger.info(s"stuck deletion reconciliation complete: $count job(s) reconciled")
    }
  }
}
