package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service

import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.UploadJob
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.UploadJobRepository
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
    * user's upload history
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
      } finally {
        finalizeInProgress.set(false)
      }
    }
  }
}
