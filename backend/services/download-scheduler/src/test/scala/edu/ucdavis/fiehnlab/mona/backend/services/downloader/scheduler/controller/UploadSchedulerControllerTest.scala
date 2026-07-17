package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.controller

import com.jayway.restassured.RestAssured
import com.jayway.restassured.builder.MultiPartSpecBuilder
import com.jayway.restassured.specification.{MultiPartSpecification, RequestSpecification}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{DeletionJob, UploadJob, UploadJobRequest}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{DeletionJobRepository, UploadJobRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.DownloadScheduler
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service.UploadStorageService
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import java.util.{Date, UUID}
import scala.jdk.CollectionConverters._

@SpringBootTest(classes = Array(classOf[DownloadScheduler]), webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class UploadSchedulerControllerTest extends AbstractSpringControllerTest {

  @LocalServerPort
  private val port = 0

  @Autowired
  val uploadJobRepository: UploadJobRepository = null

  @Autowired
  val deletionJobRepository: DeletionJobRepository = null

  @Autowired
  val uploadStorageService: UploadStorageService = null

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  @Autowired
  @Qualifier("spectra-upload-queue")
  val uploadQueueName: String = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  // RestAssured.baseURI is a single JVM-wide static also mutated by DownloadSchedulerControllerTest and
  // StaticDownloadControllerTest (both point it at /rest/downloads), and all three suites share one cached
  // Spring context in the same JVM fork, so whichever suite's assignment runs last wins for everyone; scope
  // the base URI per request instead of relying on that shared, racy static
  val baseUri: String = s"http://localhost:$port/rest/uploads"

  def given(): RequestSpecification = RestAssured.given().baseUri(baseUri)

  override def authenticate(user: String, password: String): RequestSpecification =
    super.authenticate(user, password).baseUri(baseUri)

  // Content sent in two chunks to exercise offset based assembly
  val chunkOne: Array[Byte] = "BEGIN IONS\nTITLE=first half\n".getBytes
  val chunkTwo: Array[Byte] = "100.0 200.0\nEND IONS\n".getBytes
  val totalSize: Long = chunkOne.length + chunkTwo.length

  def chunkPart(bytes: Array[Byte]): MultiPartSpecification =
    new MultiPartSpecBuilder(bytes).fileName("chunk").controlName("chunk").build()

  "UploadSchedulerControllerTest" should {
    "start with an empty job table" in {
      uploadJobRepository.deleteAll()
    }

    "reject an init request without authentication" in {
      given().contentType("application/json; charset=UTF-8")
        .body(Map("fileName" -> "test.mgf", "fileSize" -> totalSize, "format" -> "mgf"))
        .when().post("").`then`().statusCode(401)
    }

    "reject an init request without a file name" in {
      authenticate().contentType("application/json; charset=UTF-8")
        .body(Map("fileSize" -> totalSize))
        .when().post("").`then`().statusCode(400)
    }

    var jobId: String = null

    "initialize an upload job" in {
      val job: UploadJob = authenticate().contentType("application/json; charset=UTF-8")
        .body(Map("fileName" -> "test.mgf", "fileSize" -> totalSize, "format" -> "mgf", "libraryName" -> "test library"))
        .when().post("").`then`().statusCode(201).extract().body().as(classOf[UploadJob])

      assert(job.getId != null)
      assert(job.getStatus == UploadJob.STATUS_UPLOADING)
      assert(job.getEmailAddress == "admin")
      assert(job.getFileSize == totalSize)
      assert(job.getLibraryName == "test library")
      jobId = job.getId
    }

    "reject a chunk from a different non admin user" in {
      authenticate("test", "test-secret").contentType("multipart/form-data")
        .multiPart(chunkPart(chunkOne)).queryParam("offset", 0)
        .when().put(s"/$jobId/chunk").`then`().statusCode(403)
    }

    "accept the first chunk at offset 0" in {
      val job: UploadJob = authenticate().contentType("multipart/form-data")
        .multiPart(chunkPart(chunkOne)).queryParam("offset", 0)
        .when().put(s"/$jobId/chunk").`then`().statusCode(200).extract().body().as(classOf[UploadJob])

      assert(job.getUploadedBytes == chunkOne.length.toLong)
    }

    "refuse to complete while bytes are missing" in {
      authenticate().when().post(s"/$jobId/complete").`then`().statusCode(400)
    }

    "accept a resent first chunk without changing progress" in {
      val job: UploadJob = authenticate().contentType("multipart/form-data")
        .multiPart(chunkPart(chunkOne)).queryParam("offset", 0)
        .when().put(s"/$jobId/chunk").`then`().statusCode(200).extract().body().as(classOf[UploadJob])

      assert(job.getUploadedBytes == chunkOne.length.toLong)
    }

    "accept the second chunk at the next offset" in {
      val job: UploadJob = authenticate().contentType("multipart/form-data")
        .multiPart(chunkPart(chunkTwo)).queryParam("offset", chunkOne.length)
        .when().put(s"/$jobId/chunk").`then`().statusCode(200).extract().body().as(classOf[UploadJob])

      assert(job.getUploadedBytes == totalSize)
    }

    "assemble the file on disk with the exact expected content" in {
      assert(uploadStorageService.assembledSize(jobId, "test.mgf") == totalSize)
    }

    "complete the upload and schedule it for processing" in {
      val job: UploadJob = authenticate()
        .when().post(s"/$jobId/complete").`then`().statusCode(202).extract().body().as(classOf[UploadJob])

      assert(job.getStatus == UploadJob.STATUS_SCHEDULED)
      assert(job.getUploadedBytes == totalSize)
    }

    "report job status to its owner" in {
      val job: UploadJob = authenticate()
        .when().get(s"/$jobId").`then`().statusCode(200).extract().body().as(classOf[UploadJob])

      // The stub listener may have already picked the job up off the queue
      assert(job.getStatus == UploadJob.STATUS_SCHEDULED || job.getStatus == UploadJob.STATUS_RUNNING || job.getStatus == UploadJob.STATUS_COMPLETE)
    }

    "hide the job from a different non admin user" in {
      authenticate("test", "test-secret").when().get(s"/$jobId").`then`().statusCode(403)
    }

    var interruptedJobId: String = null

    "initialize a second job to exercise the interrupted to uploading revive" in {
      val job: UploadJob = authenticate().contentType("application/json; charset=UTF-8")
        .body(Map("fileName" -> "resume.mgf", "fileSize" -> totalSize, "format" -> "mgf"))
        .when().post("").`then`().statusCode(201).extract().body().as(classOf[UploadJob])

      interruptedJobId = job.getId
    }

    "revive a job from INTERRUPTED to UPLOADING when a chunk lands" in {
      val stale: UploadJob = uploadJobRepository.findById(interruptedJobId).get()
      stale.setStatus(UploadJob.STATUS_INTERRUPTED)
      uploadJobRepository.save(stale)

      val job: UploadJob = authenticate().contentType("multipart/form-data")
        .multiPart(chunkPart(chunkOne)).queryParam("offset", 0)
        .when().put(s"/$interruptedJobId/chunk").`then`().statusCode(200).extract().body().as(classOf[UploadJob])

      assert(job.getStatus == UploadJob.STATUS_UPLOADING)
    }

    "find only jobs whose lastUpdated is older than the sweep threshold" in {
      val backdated: UploadJob = uploadJobRepository.findById(interruptedJobId).get()
      backdated.setStatus(UploadJob.STATUS_UPLOADING)
      backdated.setLastUpdated(new Date(System.currentTimeMillis() - 10 * 60 * 1000L))
      uploadJobRepository.save(backdated)

      val freshJob: UploadJob = authenticate().contentType("application/json; charset=UTF-8")
        .body(Map("fileName" -> "fresh.mgf", "fileSize" -> totalSize, "format" -> "mgf"))
        .when().post("").`then`().statusCode(201).extract().body().as(classOf[UploadJob])

      val threshold = new Date(System.currentTimeMillis() - 5 * 60 * 1000L)
      val found = uploadJobRepository.findByStatusAndLastUpdatedBefore(UploadJob.STATUS_UPLOADING, threshold).asScala.map(_.getId)

      assert(found.contains(interruptedJobId))
      assert(!found.contains(freshJob.getId))

      authenticate().when().delete(s"/${freshJob.getId}").`then`().statusCode(200)
    }

    "clean up the resume test job" in {
      authenticate().when().delete(s"/$interruptedJobId").`then`().statusCode(200)
    }

    "record an interactive upload as a completed history entry" in {
      val job: UploadJob = authenticate().contentType("application/json; charset=UTF-8")
        .body(Map("fileName" -> "small.msp", "total" -> 5, "persisted" -> 4, "failed" -> 1))
        .when().post("/record").`then`().statusCode(201).extract().body().as(classOf[UploadJob])

      assert(job.getStatus == UploadJob.STATUS_COMPLETE)
      assert(job.getTotal == 5L)
      assert(job.getPersisted == 4L)
      assert(job.getFailed == 1L)
    }

    "list the caller's upload history newest first" in {
      val jobs: Array[UploadJob] = authenticate()
        .when().get("").`then`().statusCode(200).extract().body().as(classOf[Array[UploadJob]])

      assert(jobs.length == 2)
      assert(jobs.exists(_.getId == jobId))
      assert(jobs.forall(_.getEmailAddress == "admin"))
    }

    "not list another user's uploads" in {
      val jobs: Array[UploadJob] = authenticate("test", "test-secret")
        .when().get("").`then`().statusCode(200).extract().body().as(classOf[Array[UploadJob]])

      assert(jobs.isEmpty)
    }

    "record an interrupted interactive upload as a failed history entry" in {
      val job: UploadJob = authenticate().contentType("application/json; charset=UTF-8")
        .body(Map("fileName" -> "interrupted.msp", "total" -> 10, "persisted" -> 3, "failed" -> 1,
          "status" -> UploadJob.STATUS_FAILED, "errorMessage" -> "Upload was interrupted, delete and retry"))
        .when().post("/record").`then`().statusCode(201).extract().body().as(classOf[UploadJob])

      assert(job.getStatus == UploadJob.STATUS_FAILED)
      assert(job.getTotal == 10L)
      // For an interrupted upload parsed reflects what was attempted, not the full total
      assert(job.getParsed == 3L)
      assert(job.getPersisted == 3L)
      assert(job.getFailed == 1L)
      assert(job.getErrorMessage == "Upload was interrupted, delete and retry")
    }

    "coerce any non terminal record status to COMPLETE" in {
      val job: UploadJob = authenticate().contentType("application/json; charset=UTF-8")
        .body(Map("fileName" -> "sneaky.msp", "total" -> 2, "persisted" -> 2, "status" -> UploadJob.STATUS_RUNNING))
        .when().post("/record").`then`().statusCode(201).extract().body().as(classOf[UploadJob])

      assert(job.getStatus == UploadJob.STATUS_COMPLETE)
    }

    "refuse to delete another user's job" in {
      authenticate("test", "test-secret").when().delete(s"/$jobId").`then`().statusCode(403)
    }

    "delete the job and its stored file" in {
      authenticate().when().delete(s"/$jobId").`then`().statusCode(200)

      authenticate().when().get(s"/$jobId").`then`().statusCode(404)
      assert(!uploadStorageService.fileExists(jobId, "test.mgf"))
    }

    var deletingJobId: String = null

    "return not found for deletion status of an unknown upload" in {
      authenticate().when().get("/no-such-job/deletion").`then`().statusCode(404)
    }

    "return not found for an upload with no spectra deletion behind it" in {
      val job = new UploadJob(UUID.randomUUID.toString, "admin", "kept.mgf", null, "mgf", totalSize, null, new Date, UploadJob.STATUS_COMPLETE)
      uploadJobRepository.save(job)

      authenticate().when().get(s"/${job.getId}/deletion").`then`().statusCode(404)
    }

    "return the deletion job tracking an upload's spectra deletion" in {
      // Rows created directly with no queue message behind them, so the deletion listener wired
      // into this test context cannot race the progress assertions
      val job = new UploadJob(UUID.randomUUID.toString, "admin", "deleting.mgf", null, "mgf", totalSize, null, new Date, UploadJob.STATUS_DELETING)
      uploadJobRepository.save(job)
      deletingJobId = job.getId

      val deletionJob = new DeletionJob(UUID.randomUUID.toString,
        "exists(metaData.name:'origin' and metaData.value:'deleting.mgf') and submitter.emailAddress:'admin'",
        null, "admin", new Date, DeletionJob.STATUS_RUNNING, 10L)
      deletionJob.setDeleted(4L)
      deletionJob.setSkipped(1L)
      deletionJob.setUploadJobId(job.getId)
      deletionJobRepository.save(deletionJob)

      val fetched: DeletionJob = authenticate()
        .when().get(s"/$deletingJobId/deletion").`then`().statusCode(200).extract().body().as(classOf[DeletionJob])

      assert(fetched.getId == deletionJob.getId)
      assert(fetched.getStatus == DeletionJob.STATUS_RUNNING)
      assert(fetched.getTotal == 10L)
      assert(fetched.getDeleted == 4L)
      assert(fetched.getSkipped == 1L)
    }

    "refuse deletion status without authentication" in {
      given().when().get(s"/$deletingJobId/deletion").`then`().statusCode(401)
    }

    "refuse deletion status for another user's upload" in {
      authenticate("test", "test-secret").when().get(s"/$deletingJobId/deletion").`then`().statusCode(403)
    }

    var cancelJobId: String = null

    "initialize a job to exercise cancellation" in {
      val job: UploadJob = authenticate().contentType("application/json; charset=UTF-8")
        .body(Map("fileName" -> "cancel.mgf", "fileSize" -> totalSize, "format" -> "mgf"))
        .when().post("").`then`().statusCode(201).extract().body().as(classOf[UploadJob])
      cancelJobId = job.getId

      authenticate().contentType("multipart/form-data")
        .multiPart(chunkPart(chunkOne)).queryParam("offset", 0)
        .when().put(s"/$cancelJobId/chunk").`then`().statusCode(200)
    }

    "refuse to cancel without authentication" in {
      given().when().post(s"/$cancelJobId/cancel").`then`().statusCode(401)
    }

    "refuse to cancel another user's job" in {
      authenticate("test", "test-secret").when().post(s"/$cancelJobId/cancel").`then`().statusCode(403)
    }

    "return not found when cancelling an unknown job" in {
      authenticate().when().post("/no-such-job/cancel").`then`().statusCode(404)
    }

    "cancel an UPLOADING job directly and remove its stored file" in {
      val job: UploadJob = authenticate()
        .when().post(s"/$cancelJobId/cancel").`then`().statusCode(200).extract().body().as(classOf[UploadJob])

      assert(job.getStatus == UploadJob.STATUS_CANCELLED)
      assert(!uploadStorageService.fileExists(cancelJobId, "cancel.mgf"))
    }

    "refuse further chunks for a cancelled job" in {
      authenticate().contentType("multipart/form-data")
        .multiPart(chunkPart(chunkTwo)).queryParam("offset", chunkOne.length)
        .when().put(s"/$cancelJobId/chunk").`then`().statusCode(409)
    }

    "refuse to schedule a cancelled job" in {
      authenticate().when().post(s"/$cancelJobId/complete").`then`().statusCode(409)
    }

    "refuse to cancel an already cancelled job" in {
      authenticate().when().post(s"/$cancelJobId/cancel").`then`().statusCode(409)
    }

    "refuse to cancel a completed job" in {
      val job = new UploadJob(UUID.randomUUID.toString, "admin", "done.mgf", null, "mgf", totalSize, null, new Date, UploadJob.STATUS_COMPLETE)
      uploadJobRepository.save(job)

      authenticate().when().post(s"/${job.getId}/cancel").`then`().statusCode(409)
    }

    "cancel an INTERRUPTED job directly" in {
      val job = new UploadJob(UUID.randomUUID.toString, "admin", "stale.mgf", null, "mgf", totalSize, null, new Date, UploadJob.STATUS_INTERRUPTED)
      uploadJobRepository.save(job)

      val cancelled: UploadJob = authenticate()
        .when().post(s"/${job.getId}/cancel").`then`().statusCode(200).extract().body().as(classOf[UploadJob])

      assert(cancelled.getStatus == UploadJob.STATUS_CANCELLED)
    }

    "flip a RUNNING job to CANCELLING for the worker to finalize" in {
      // Created directly at RUNNING with no queue message behind it, so the status assertion
      // cannot race the real listener wired into this test context
      val job = new UploadJob(UUID.randomUUID.toString, "admin", "running.mgf", null, "mgf", totalSize, null, new Date, UploadJob.STATUS_RUNNING)
      uploadJobRepository.save(job)

      val cancelling: UploadJob = authenticate()
        .when().post(s"/${job.getId}/cancel").`then`().statusCode(200).extract().body().as(classOf[UploadJob])

      assert(cancelling.getStatus == UploadJob.STATUS_CANCELLING)
    }

    "finalize a CANCELLING job at message receipt without parsing" in {
      val job = new UploadJob(UUID.randomUUID.toString, "admin", "queued.mgf", null, "mgf", totalSize, null, new Date, UploadJob.STATUS_CANCELLING)
      uploadJobRepository.save(job)

      rabbitTemplate.convertAndSend(uploadQueueName, new UploadJobRequest(job.getId))

      val deadline = System.currentTimeMillis() + 15000
      var status = job.getStatus
      while (status != UploadJob.STATUS_CANCELLED && System.currentTimeMillis() < deadline) {
        Thread.sleep(500)
        status = uploadJobRepository.findById(job.getId).get().getStatus
      }
      assert(status == UploadJob.STATUS_CANCELLED)
      assert(uploadJobRepository.findById(job.getId).get().getParsed == 0L)
    }
  }
}
