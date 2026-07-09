package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.deletion

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{DeletionJob, SpectrumDeletionRequest, UploadJob}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{DeletionJobRepository, UploadJobRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import org.springframework.beans.factory.annotation.Autowired

import java.util.Date
import scala.jdk.CollectionConverters._

/**
  * Consumes spectrum deletion requests off the durable deletion queue and runs the batched,
  * progress-tracked delete in the background. The DeletionJob row carries the work payload and
  * progress, so the actual deletion is decoupled from the original HTTP request and survives a
  * service restart.
  *
  * Registered as a bean by DeletionQueueConfig (not a scanned component) so that importing that
  * one config wires both the listener and its queue container, in production and under test alike
  */
class SpectrumDeletionListener extends GenericMessageListener[SpectrumDeletionRequest] with LazyLogging {

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  val deletionJobRepository: DeletionJobRepository = null

  @Autowired
  val uploadJobRepository: UploadJobRepository = null

  // Tells the uploadJob row to transition to DELETED once the deletionJob is complete
  // Edge case: if the deletionJob is FAILED, the uploadJob stays in the DELETING state
  private def closeUploadJobLoop(job: DeletionJob): Unit = {
    if (job.getUploadJobId != null && job.getStatus == DeletionJob.STATUS_COMPLETE) {
      try {
        val uploadJob: UploadJob = uploadJobRepository.findById(job.getUploadJobId).orElse(null)

        if (uploadJob != null) {
          uploadJob.setStatus(UploadJob.STATUS_DELETED)
          uploadJob.setDeletedDate(new Date())
          uploadJobRepository.save(uploadJob)
        } else {
          logger.warn(s"deletion job ${job.getId} references upload job ${job.getUploadJobId}, which no longer exists")
        }
      } catch {
        // Never let a failure here escape handleMessage: the deletion itself already succeeded
        case e: Exception =>
          logger.error(s"failed to close the loop on upload job ${job.getUploadJobId} for deletion job ${job.getId}: ${e.getMessage}", e)
      }
    }
  }

  override def handleMessage(request: SpectrumDeletionRequest): Unit = {
    val jobId: String = request.getJobId
    logger.info(s"received spectrum deletion request for job $jobId")

    val job: DeletionJob = deletionJobRepository.findById(jobId).orElse(null)

    if (job == null) {
      logger.error(s"no deletion job found for id $jobId, ignoring request")
    } else {
      try {
        job.setStatus(DeletionJob.STATUS_RUNNING)
        job.setLastUpdated(new Date())
        deletionJobRepository.save(job)

        if (job.getQuery != null && job.getQuery.trim.nonEmpty) {
          logger.info(s"deleting spectra for job $jobId by query: ${job.getQuery}")
          spectrumPersistenceService.deleteSpectraByQueryTracked(job.getQuery, job)
        } else if (job.getSpectrumIds != null && job.getSpectrumIds.trim.nonEmpty) {
          val ids = job.getSpectrumIds.split(",").map(_.trim).filter(_.nonEmpty).toList
          logger.info(s"deleting ${ids.size} spectra for job $jobId by id")
          spectrumPersistenceService.deleteSpectraByIdsTracked(ids.asJava, job)
        } else {
          logger.warn(s"deletion job $jobId has neither a query nor ids, nothing to delete")
        }

        job.setStatus(DeletionJob.STATUS_COMPLETE)
        job.setLastUpdated(new Date())
        deletionJobRepository.save(job)
        logger.info(s"deletion job $jobId complete: deleted ${job.getDeleted}, skipped ${job.getSkipped}")
      } catch {
        case e: Exception =>
          logger.error(s"deletion job $jobId failed: ${e.getMessage}", e)
          job.setStatus(DeletionJob.STATUS_FAILED)
          job.setErrorMessage(e.getMessage)
          job.setLastUpdated(new Date())
          deletionJobRepository.save(job)
      }

      closeUploadJobLoop(job)
    }
  }
}
