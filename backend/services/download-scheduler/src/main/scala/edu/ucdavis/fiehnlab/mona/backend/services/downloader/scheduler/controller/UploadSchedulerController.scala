package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.controller

import java.util.{Date, UUID}

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginInfo
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{DeletionJob, SpectrumDeletionRequest, UploadJob, UploadJobRequest}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{DeletionJobRepository, UploadJobRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service.UploadStorageService
import org.springframework.amqp.core.{Message, MessageDeliveryMode}
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation._
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpServletRequest
import scala.jdk.CollectionConverters._

/**
  * Accepts spectra file uploads, stores them on disk in resumable chunks, and tracks each upload
  * as an UploadJob so progress and history survive a closed tab or a service restart. Once a file
  * is fully assembled it is enqueued on the durable upload queue for background parsing
  *
  * Uploads are tied to the caller resolved from the bearer token. A user sees and deletes only
  * their own jobs unless they are an admin, mirroring the ownership checks on spectrum delete
  */
@RestController
@RequestMapping(value = Array("/rest/uploads"))
class UploadSchedulerController extends LazyLogging {

  @Autowired
  val uploadJobRepository: UploadJobRepository = null

  @Autowired
  val deletionJobRepository: DeletionJobRepository = null

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  @Qualifier("spectra-deletion-queue")
  val deletionQueueName: String = null

  @Autowired
  val uploadStorageService: UploadStorageService = null

  @Autowired
  val loginService: LoginService = null

  @Autowired
  val httpServletRequest: HttpServletRequest = null

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  @Autowired
  @Qualifier("spectra-upload-queue")
  val uploadQueueName: String = null

  @Autowired
  val objectMapper: ObjectMapper = null

  // Defense in depth against a single uploaded file larger than 10gb
  @Value("${mona.uploads.max-file-size-bytes:10737418240}")
  val maxFileSizeBytes: Long = 10737418240L

  /* 30gb max storage size for the docker container's volume (mona_uploads), prevent gose from filling
   * A request that would exceed this limit returns 507 INSUFFICIENT_STORAGE
   * This should never actually happen since the volume is cleaned automatically 
   */
  @Value("${mona.uploads.max-total-size-bytes:32212254720}")
  val maxTotalSizeBytes: Long = 32212254720L

  // Resolves the caller's login info from the Authorization header, or null when no usable token is present
  private def callerInfo(): LoginInfo = {
    val header = httpServletRequest.getHeader("Authorization")

    if (header != null && header.split(" ").length > 1) {
      try {
        loginService.info(header.split(" ").last)
      } catch {
        case e: Exception =>
          logger.debug(s"could not resolve caller: ${e.getMessage}")
          null
      }
    } else {
      null
    }
  }

  private def canAccess(job: UploadJob, info: LoginInfo): Boolean = {
    info != null && (info.roles.contains("ADMIN") || job.getEmailAddress == info.emailAddress)
  }

  // A single spectrum interactive upload's history row is labeled with its server assigned spectrum
  // id instead of a filename (see spectra-upload.component.ts's matching SPECTRUM_FILENAME_PATTERN)
  private val SpectrumFileNamePattern = """^Spectrum (\S+)$""".r

  // What deleting this job's spectra actually targets: either one known spectrum id, or an RSQL
  // query. Neither field set means there is nothing to delete
  private case class SpectraDeletionTarget(query: Option[String], ids: List[String]) {
    def isEmpty: Boolean = query.isEmpty && ids.isEmpty
  }

  /**
    * Resolves job.fileName into what should be deleted.
    * An OR across origin file names alone could also match a different
    * user's identically named upload, so the query additionally ANDs on this job's submitter.
    * That submitter is job.getLibrarySubmitterEmail if the library form's override was used, else job.getEmailAddress
    */
  private def resolveDeletionTarget(job: UploadJob): SpectraDeletionTarget = {
    Option(job.getFileName).map(_.trim).filter(_.nonEmpty) match {
      case None => SpectraDeletionTarget(None, Nil)
      case Some(SpectrumFileNamePattern(spectrumId)) => SpectraDeletionTarget(None, List(spectrumId))
      case Some(fileName) =>
        val originNames = fileName.split(",").map(_.trim).filter(_.nonEmpty)

        if (originNames.isEmpty) {
          SpectraDeletionTarget(None, Nil)
        } else {
          val submitterEmail = Option(job.getLibrarySubmitterEmail).filter(_.nonEmpty).getOrElse(job.getEmailAddress)
          val originClauses = originNames.map(name => s"exists(metaData.name:'origin' and metaData.value:'$name')").mkString(" or ")
          SpectraDeletionTarget(Some(s"($originClauses) and submitter.emailAddress:'$submitterEmail'"), Nil)
        }
    }
  }

  private def countDeletionTarget(target: SpectraDeletionTarget): Long = target match {
    case SpectraDeletionTarget(Some(query), _) => spectrumPersistenceService.count(query)
    case SpectraDeletionTarget(None, ids) => ids.size.toLong
  }

  /**
    * Enqueues a tracked deletion job for the given target, linked back to this upload job so
    * SpectrumDeletionListener can flip it to DELETED once the deletion completes
    */
  private def enqueueSpectraDeletion(job: UploadJob, target: SpectraDeletionTarget, triggeredBy: String): Unit = {
    val total = countDeletionTarget(target)
    val deletionJob = target match {
      case SpectraDeletionTarget(Some(query), _) =>
        new DeletionJob(UUID.randomUUID.toString, query, null, triggeredBy, new Date, DeletionJob.STATUS_SCHEDULED, total)
      case SpectraDeletionTarget(None, ids) =>
        new DeletionJob(UUID.randomUUID.toString, null, ids.mkString(","), triggeredBy, new Date, DeletionJob.STATUS_SCHEDULED, total)
    }
    deletionJob.setUploadJobId(job.getId)
    deletionJobRepository.save(deletionJob)

    rabbitTemplate.convertAndSend(deletionQueueName, new SpectrumDeletionRequest(deletionJob.getId), (message: Message) => {
      message.getMessageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT)
      message
    })
  }

  /**
    * Initializes an upload: records the file metadata, creates the on disk job directory, and
    * returns the new UploadJob (status UPLOADING) whose id the client uses for the chunk uploads
    */
  @RequestMapping(method = Array(RequestMethod.POST))
  def initUpload(@RequestBody payload: java.util.Map[String, Object]): ResponseEntity[UploadJob] = {
    val info: LoginInfo = callerInfo()

    if (info == null) {
      new ResponseEntity[UploadJob](HttpStatus.UNAUTHORIZED)
    } else {
      val fileName: String = Option(payload.get("fileName")).map(_.toString).orNull
      val format: String = Option(payload.get("format")).map(_.toString).orNull
      val libraryName: String = Option(payload.get("libraryName")).map(_.toString).orNull
      val libraryDescription: String = Option(payload.get("libraryDescription")).map(_.toString).orNull
      val libraryLink: String = Option(payload.get("libraryLink")).map(_.toString).orNull
      val libraryPrefix: String = Option(payload.get("libraryPrefix")).map(_.toString).orNull
      val librarySubmitterEmail: String = Option(payload.get("librarySubmitterEmail")).map(_.toString).orNull
      val librarySubmitterFirstName: String = Option(payload.get("librarySubmitterFirstName")).map(_.toString).orNull
      val librarySubmitterLastName: String = Option(payload.get("librarySubmitterLastName")).map(_.toString).orNull
      val librarySubmitterInstitution: String = Option(payload.get("librarySubmitterInstitution")).map(_.toString).orNull
      // Spring already deserialized the JSON body's "additionalTags" array into a java.util.List;
      // re-serialize it to a single JSON string since UploadJob has no collection valued columns
      val additionalTags: String = Option(payload.get("additionalTags")).map(objectMapper.writeValueAsString).orNull
      val fileSize: java.lang.Long =
        Option(payload.get("fileSize")).map(v => java.lang.Long.valueOf(v.toString.toLong)).orNull

      // Measured once here rather than per chunk so the volume walk stays off the hot upload path
      val currentTotal: Long = uploadStorageService.totalSize()

      if (fileName == null || fileName.trim.isEmpty) {
        new ResponseEntity[UploadJob](HttpStatus.BAD_REQUEST)
      } else if (fileSize != null && fileSize > maxFileSizeBytes) {
        logger.warn(s"rejected upload init for ${info.emailAddress}: $fileName ($fileSize bytes) exceeds the $maxFileSizeBytes byte cap")
        new ResponseEntity[UploadJob](HttpStatus.PAYLOAD_TOO_LARGE)
      } else if (fileSize != null && currentTotal + fileSize > maxTotalSizeBytes) {
        logger.warn(s"rejected upload init for ${info.emailAddress}: $fileName ($fileSize bytes) would push the uploads volume past its $maxTotalSizeBytes byte cap (currently $currentTotal bytes used)")
        new ResponseEntity[UploadJob](HttpStatus.INSUFFICIENT_STORAGE)
      } else {
        val jobId: String = UUID.randomUUID.toString
        uploadStorageService.initJob(jobId)
        val storedPath: String = uploadStorageService.storedFile(jobId, fileName).toString

        val job: UploadJob = new UploadJob(jobId, info.emailAddress, fileName, storedPath, format, fileSize, libraryName, new Date, UploadJob.STATUS_UPLOADING)
        job.setLibraryDescription(libraryDescription)
        job.setLibraryLink(libraryLink)
        job.setLibraryPrefix(libraryPrefix)
        job.setLibrarySubmitterEmail(librarySubmitterEmail)
        job.setLibrarySubmitterFirstName(librarySubmitterFirstName)
        job.setLibrarySubmitterLastName(librarySubmitterLastName)
        job.setLibrarySubmitterInstitution(librarySubmitterInstitution)
        job.setAdditionalTags(additionalTags)
        uploadJobRepository.save(job)

        logger.info(s"initialized upload job $jobId for ${info.emailAddress}: $fileName ($fileSize bytes)")
        new ResponseEntity[UploadJob](job, HttpStatus.CREATED)
      }
    }
  }

  /**
    * Records a client side interactive upload (the small file editor path that parses and posts
    * spectra directly) as a history entry. There is no stored file to parse, this only gives
    * those uploads a row in the user's upload history alongside server side uploads. Normally
    * recorded as COMPLETE when the batch finishes, or as FAILED with an error message when the
    * client detects that a page refresh or close interrupted the batch
    */
  @RequestMapping(path = Array("/record"), method = Array(RequestMethod.POST))
  def recordInteractive(@RequestBody payload: java.util.Map[String, Object]): ResponseEntity[UploadJob] = {
    val info: LoginInfo = callerInfo()

    if (info == null) {
      new ResponseEntity[UploadJob](HttpStatus.UNAUTHORIZED)
    } else {
      def longOf(key: String): Long = Option(payload.get(key)).map(_.toString.toLong).getOrElse(0L)

      val fileName: String = Option(payload.get("fileName")).map(_.toString).getOrElse("Interactive upload")
      val libraryName: String = Option(payload.get("libraryName")).map(_.toString).orNull
      val librarySubmitterEmail: String = Option(payload.get("librarySubmitterEmail")).map(_.toString).orNull
      val librarySubmitterFirstName: String = Option(payload.get("librarySubmitterFirstName")).map(_.toString).orNull
      val librarySubmitterLastName: String = Option(payload.get("librarySubmitterLastName")).map(_.toString).orNull
      val librarySubmitterInstitution: String = Option(payload.get("librarySubmitterInstitution")).map(_.toString).orNull

      // Only the two terminal states may be recorded, anything else is coerced to COMPLETE
      val status: String = Option(payload.get("status")).map(_.toString) match {
        case Some(UploadJob.STATUS_FAILED) => UploadJob.STATUS_FAILED
        case _ => UploadJob.STATUS_COMPLETE
      }

      val job: UploadJob = new UploadJob(UUID.randomUUID.toString, info.emailAddress, fileName, null, null, null, libraryName, new Date, status)
      job.setTotal(longOf("total"))
      // An interrupted upload only got through part of the batch, so parsed reflects what was
      // attempted rather than the full total
      job.setParsed(if (status == UploadJob.STATUS_FAILED) longOf("persisted") else longOf("total"))
      job.setPersisted(longOf("persisted"))
      job.setFailed(longOf("failed"))
      job.setErrorMessage(Option(payload.get("errorMessage")).map(_.toString).orNull)
      // Carry optional submitter if there was one
      job.setLibrarySubmitterEmail(librarySubmitterEmail)
      job.setLibrarySubmitterFirstName(librarySubmitterFirstName)
      job.setLibrarySubmitterLastName(librarySubmitterLastName)
      job.setLibrarySubmitterInstitution(librarySubmitterInstitution)
      uploadJobRepository.save(job)

      logger.info(s"recorded interactive upload for ${info.emailAddress}: $fileName (${job.getPersisted}/${job.getTotal}, $status)")
      new ResponseEntity[UploadJob](job, HttpStatus.CREATED)
    }
  }

  /**
    * Appends a chunk at the given byte offset. Re-sending the same offset overwrites, so a dropped
    * connection is recovered simply by resending. Returns the updated job so the client sees the
    * committed byte count
    */
  @RequestMapping(path = Array("/{jobId}/chunk"), method = Array(RequestMethod.PUT))
  def uploadChunk(@PathVariable("jobId") jobId: String,
                  @RequestParam("offset") offset: Long,
                  @RequestParam("chunk") chunk: MultipartFile): ResponseEntity[UploadJob] = {
    val info: LoginInfo = callerInfo()
    val job: UploadJob = uploadJobRepository.findById(jobId).orElse(null)

    if (job == null) {
      new ResponseEntity[UploadJob](HttpStatus.NOT_FOUND)
    } else if (!canAccess(job, info)) {
      new ResponseEntity[UploadJob](HttpStatus.FORBIDDEN)
    } else {
      val newEnd: Long = uploadStorageService.writeChunk(jobId, job.getFileName, offset, chunk)
      // Track the furthest committed byte so an out of order or resent chunk never shrinks progress
      if (job.getUploadedBytes == null || newEnd > job.getUploadedBytes) {
        job.setUploadedBytes(newEnd)
      }
      // A chunk landing on a job the sweep flagged as stale is itself the resume signal
      if (job.getStatus == UploadJob.STATUS_INTERRUPTED) {
        job.setStatus(UploadJob.STATUS_UPLOADING)
        logger.info(s"upload job $jobId resumed after being interrupted")
      }
      job.setLastUpdated(new Date())
      uploadJobRepository.save(job)

      new ResponseEntity[UploadJob](job, HttpStatus.OK)
    }
  }

  /**
    * Marks the upload complete once the whole file has arrived: verifies the assembled size, flips
    * the job to SCHEDULED, and enqueues it for background parsing with persistent delivery so it
    * survives a broker restart
    */
  @RequestMapping(path = Array("/{jobId}/complete"), method = Array(RequestMethod.POST))
  def completeUpload(@PathVariable("jobId") jobId: String): ResponseEntity[UploadJob] = {
    val info: LoginInfo = callerInfo()
    val job: UploadJob = uploadJobRepository.findById(jobId).orElse(null)

    if (job == null) {
      new ResponseEntity[UploadJob](HttpStatus.NOT_FOUND)
    } else if (!canAccess(job, info)) {
      new ResponseEntity[UploadJob](HttpStatus.FORBIDDEN)
    } else {
      val assembled: Long = uploadStorageService.assembledSize(jobId, job.getFileName)

      if (job.getFileSize != null && assembled != job.getFileSize) {
        logger.warn(s"upload job $jobId incomplete: assembled $assembled of ${job.getFileSize} bytes")
        new ResponseEntity[UploadJob](job, HttpStatus.BAD_REQUEST)
      } else {
        job.setUploadedBytes(assembled)
        job.setStatus(UploadJob.STATUS_SCHEDULED)
        job.setLastUpdated(new Date())
        uploadJobRepository.save(job)

        rabbitTemplate.convertAndSend(uploadQueueName, new UploadJobRequest(job.getId), (message: Message) => {
          message.getMessageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT)
          message
        })

        logger.info(s"upload job $jobId scheduled for parsing")
        new ResponseEntity[UploadJob](job, HttpStatus.ACCEPTED)
      }
    }
  }

  /**
    * Returns the current job, used both to poll parsing progress and to read back the committed
    * byte offset when resuming an interrupted transfer
    */
  @RequestMapping(path = Array("/{jobId}"), method = Array(RequestMethod.GET))
  def getJob(@PathVariable("jobId") jobId: String): ResponseEntity[UploadJob] = {
    val info: LoginInfo = callerInfo()
    val job: UploadJob = uploadJobRepository.findById(jobId).orElse(null)

    if (job == null) {
      new ResponseEntity[UploadJob](HttpStatus.NOT_FOUND)
    } else if (!canAccess(job, info)) {
      new ResponseEntity[UploadJob](HttpStatus.FORBIDDEN)
    } else {
      new ResponseEntity[UploadJob](job, HttpStatus.OK)
    }
  }

  /**
    * Upload history for the caller, newest first
    */
  @RequestMapping(method = Array(RequestMethod.GET))
  def listMyJobs(): ResponseEntity[java.util.List[UploadJob]] = {
    val info: LoginInfo = callerInfo()

    if (info == null) {
      new ResponseEntity[java.util.List[UploadJob]](HttpStatus.UNAUTHORIZED)
    } else {
      val jobs: java.util.List[UploadJob] = uploadJobRepository.findByEmailAddressOrderByDateDesc(info.emailAddress)
      new ResponseEntity[java.util.List[UploadJob]](jobs, HttpStatus.OK)
    }
  }

  /**
    * Returns the count of spectra that deleteJob's deleteSpectra=true would delete for this job,
    * so the delete confirmation can show an accurate number without actually deleting anything
    */
  @RequestMapping(path = Array("/{jobId}/spectraCount"), method = Array(RequestMethod.GET))
  def spectraCount(@PathVariable("jobId") jobId: String): ResponseEntity[java.lang.Long] = {
    val info: LoginInfo = callerInfo()
    val job: UploadJob = uploadJobRepository.findById(jobId).orElse(null)

    if (job == null) {
      new ResponseEntity[java.lang.Long](HttpStatus.NOT_FOUND)
    } else if (!canAccess(job, info)) {
      new ResponseEntity[java.lang.Long](HttpStatus.FORBIDDEN)
    } else {
      new ResponseEntity[java.lang.Long](countDeletionTarget(resolveDeletionTarget(job)), HttpStatus.OK)
    }
  }

  /**
    * Deletes an upload's stored file immediately. When deleteSpectra is true, the job's spectra
    * (see resolveDeletionTarget) are also removed via the same tracked async deletion pipeline as
    * the admin mass delete endpoints. Since deletion is async, the job row is not removed: it flips
    * to DELETING now and to DELETED (with deletedDate set) once SpectrumDeletionListener finishes,
    * so the upload history table keeps a permanent record of the deletion
    */
  @RequestMapping(path = Array("/{jobId}"), method = Array(RequestMethod.DELETE))
  def deleteJob(@PathVariable("jobId") jobId: String,
                @RequestParam(value = "deleteSpectra", required = false, defaultValue = "false") deleteSpectra: Boolean): ResponseEntity[UploadJob] = {
    val info: LoginInfo = callerInfo()
    val job: UploadJob = uploadJobRepository.findById(jobId).orElse(null)

    if (job == null) {
      new ResponseEntity[UploadJob](HttpStatus.NOT_FOUND)
    } else if (!canAccess(job, info)) {
      new ResponseEntity[UploadJob](HttpStatus.FORBIDDEN)
    } else {
      uploadStorageService.deleteJob(jobId)

      if (!deleteSpectra) {
        uploadJobRepository.delete(job)
        logger.info(s"deleted upload job $jobId (deleteSpectra=false)")
        new ResponseEntity[UploadJob](HttpStatus.OK)
      } else {
        val target = resolveDeletionTarget(job)

        if (target.isEmpty) {
          job.setStatus(UploadJob.STATUS_DELETED)
          job.setDeletedDate(new Date())
        } else {
          job.setStatus(UploadJob.STATUS_DELETING)
          enqueueSpectraDeletion(job, target, info.emailAddress)
        }

        job.setLastUpdated(new Date())
        uploadJobRepository.save(job)

        logger.info(s"deleted upload job $jobId file, spectra deletion status=${job.getStatus}")
        new ResponseEntity[UploadJob](job, HttpStatus.OK)
      }
    }
  }
}
