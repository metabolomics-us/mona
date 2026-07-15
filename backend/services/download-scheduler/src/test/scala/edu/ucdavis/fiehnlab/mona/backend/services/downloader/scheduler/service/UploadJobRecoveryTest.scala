package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{UploadJob, UploadJobRequest}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.UploadJobRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.DownloadScheduler
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.{Date, UUID}

/**
  * Covers the crash and redelivery recovery around background upload parsing: the listener's
  * idempotency guard (a redelivered request must never re-run or clobber a job that is no longer
  * SCHEDULED) and UploadJobSweepService.reconcileUploadsOnStartup, which settles every non-terminal
  * job left behind by a service restart so none can sit stuck
  */
@SpringBootTest(classes = Array(classOf[DownloadScheduler]), webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class UploadJobRecoveryTest extends AbstractSpringControllerTest {

  @Autowired
  val uploadJobRepository: UploadJobRepository = null

  @Autowired
  val uploadStorageService: UploadStorageService = null

  @Autowired
  val uploadJobSweepService: UploadJobSweepService = null

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  @Autowired
  @Qualifier("spectra-upload-queue")
  val uploadQueueName: String = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  // A minimal but readable MGF with a single spectrum, so countTotal finds one "BEGIN IONS" marker
  val oneSpectrumMgf: String = "BEGIN IONS\nTITLE=recovery test\nPEPMASS=100.0\n100.0 200.0\nEND IONS\n"

  private def saveJob(status: String, fileName: String, storedPath: String = null): String = {
    val job = new UploadJob(UUID.randomUUID.toString, "admin", fileName, storedPath, "mgf", 10L, null, new Date, status)
    job.setLastUpdated(new Date())
    uploadJobRepository.save(job)
    job.getId
  }

  private def waitForStatus(jobId: String, target: String, timeoutMs: Long): String = {
    val deadline = System.currentTimeMillis() + timeoutMs
    var status = uploadJobRepository.findById(jobId).get().getStatus
    while (status != target && System.currentTimeMillis() < deadline) {
      Thread.sleep(300)
      status = uploadJobRepository.findById(jobId).get().getStatus
    }
    status
  }

  "UploadJobRecoveryTest" should {

    "start with an empty upload table" in {
      uploadJobRepository.deleteAll()
    }

    "not re-run or clobber a COMPLETE job when its upload request is redelivered" in {
      // Reproduces the reported failure: a long upload finished, then the broker redelivered the
      // original request onto the already-deleted file and flipped the job to FAILED
      val job = new UploadJob(UUID.randomUUID.toString, "admin", "done.mgf", null, "mgf", 10L, null, new Date, UploadJob.STATUS_COMPLETE)
      job.setParsed(5L)
      job.setPersisted(5L)
      job.setTotal(5L)
      uploadJobRepository.save(job)

      rabbitTemplate.convertAndSend(uploadQueueName, new UploadJobRequest(job.getId))

      // Asserting a non event, so wait long enough for the listener to receive and ignore it
      Thread.sleep(3000)

      val after = uploadJobRepository.findById(job.getId).get()
      assert(after.getStatus == UploadJob.STATUS_COMPLETE)
      assert(after.getErrorMessage == null)
      assert(after.getPersisted == 5L)
    }

    "fail a RUNNING job and finalize a CANCELLING job on startup, leaving resumable and deleting states alone" in {
      uploadJobRepository.deleteAll()

      val running = saveJob(UploadJob.STATUS_RUNNING, "running.mgf")
      val cancelling = saveJob(UploadJob.STATUS_CANCELLING, "cancelling.mgf")
      val uploading = saveJob(UploadJob.STATUS_UPLOADING, "uploading.mgf")
      val interrupted = saveJob(UploadJob.STATUS_INTERRUPTED, "interrupted.mgf")
      val deleting = saveJob(UploadJob.STATUS_DELETING, "deleting.mgf")

      uploadJobSweepService.reconcileUploadsOnStartup()

      val runningAfter = uploadJobRepository.findById(running).get()
      assert(runningAfter.getStatus == UploadJob.STATUS_FAILED)
      assert(runningAfter.getErrorMessage == "Interrupted by a server restart, delete and retry")

      assert(uploadJobRepository.findById(cancelling).get().getStatus == UploadJob.STATUS_CANCELLED)

      // A chunk transfer still resumes, and an in flight spectra deletion is left for the weekly sweep
      assert(uploadJobRepository.findById(uploading).get().getStatus == UploadJob.STATUS_UPLOADING)
      assert(uploadJobRepository.findById(interrupted).get().getStatus == UploadJob.STATUS_INTERRUPTED)
      assert(uploadJobRepository.findById(deleting).get().getStatus == UploadJob.STATUS_DELETING)
    }

    "re-enqueue a SCHEDULED job on startup and parse its stored file without a re-upload" in {
      uploadJobRepository.deleteAll()

      val jobId = UUID.randomUUID.toString
      val fileName = "reenqueue.mgf"

      // Stage a fully assembled file where storedPath points, as if a prior run had completed the
      // chunk upload and scheduled the job but died before parsing it
      uploadStorageService.initJob(jobId)
      val stored = uploadStorageService.storedFile(jobId, fileName)
      Files.write(stored, oneSpectrumMgf.getBytes(StandardCharsets.UTF_8))

      val job = new UploadJob(jobId, "admin", fileName, stored.toString, "mgf", oneSpectrumMgf.length.toLong, null, new Date, UploadJob.STATUS_SCHEDULED)
      job.setLastUpdated(new Date())
      uploadJobRepository.save(job)

      uploadJobSweepService.reconcileUploadsOnStartup()

      // The re-enqueued request drives the live listener to reuse the staged file and parse it
      val status = waitForStatus(jobId, UploadJob.STATUS_COMPLETE, 20000)
      assert(status == UploadJob.STATUS_COMPLETE)

      val done = uploadJobRepository.findById(jobId).get()
      assert(done.getTotal == 1L)
      // A normal completion deletes the raw file
      assert(!uploadStorageService.fileExists(jobId, fileName))
    }
  }
}
