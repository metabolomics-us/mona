package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.upload

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Library, SpectrumSubmitter, UploadJob, UploadJobRequest}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.{DomainReadEventHandler, DomainReader}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.mgf.MGFReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp.MSPReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.massbank.MassBankReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.upload.{RawParsedSpectrum, UploadSpectrumBuilder}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{SubmitterRepository, UploadJobRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.{LibraryPrefixCounterService, SpectrumPersistenceService}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service.UploadStorageService
import org.springframework.beans.factory.annotation.Autowired

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.Date
import java.util.concurrent.{ExecutorService, Executors}
import javax.annotation.PreDestroy

/**
  * Consumes upload requests off the durable upload queue and walks the UploadJob through its
  * lifecycle in the background. The UploadJob row carries the file location and progress, so the
  * work is decoupled from the original HTTP request and survives a service restart.
  *
  * Parses the assembled file with the reader matching job.getFormat, persists each spectrum
  * in-process via SpectrumPersistenceService (the same call the interactive /rest/spectra path
  * makes, so curation/splash events fire identically), and deletes the raw file once every
  * spectrum has been attempted. A bad individual spectrum is skipped and counted in job.failed
  * rather than failing the whole job; only a whole-file failure (unreadable file, unknown format,
  * no submitter account) flips the job to FAILED, deleting the raw file too since a terminal
  * failure can never be resumed. A cancel request (status CANCELLING, set by the controller) is
  * honored at message receipt or at the next progress checkpoint: the job finalizes to CANCELLED
  * with the counts reached so far, keeping every spectrum already persisted and deleting the raw
  * file. Registered as a bean by UploadQueueConfig (not a scanned
  * component) so importing that one config wires both the listener and its queue container
  */
class UploadJobListener extends GenericMessageListener[UploadJobRequest] with LazyLogging {

  @Autowired
  val uploadJobRepository: UploadJobRepository = null

  @Autowired
  val submitterRepository: SubmitterRepository = null

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  val uploadStorageService: UploadStorageService = null

  @Autowired
  val libraryPrefixCounterService: LibraryPrefixCounterService = null

  // Parsing runs here rather than on the AMQP listener thread so handleMessage can return (and the
  // broker can ack) the instant a request is received. Holding the delivery open for the whole
  // parse (minutes to hours on a large library) trips RabbitMQ's consumer_timeout, which requeues
  // the message and redelivers a duplicate onto the by-then-deleted file
  private val parseExecutor: ExecutorService = Executors.newSingleThreadExecutor()

  @PreDestroy
  def shutdown(): Unit = parseExecutor.shutdown()

  // Flushed to the DB every N spectra rather than on every one
  private val ProgressFlushBatchSize = 200

  // Control flow only: thrown out of the read callback when a cancel request is seen at a
  // progress checkpoint, so the parse loop unwinds without being treated as a job failure
  private class UploadCancelledException extends Exception

  override def handleMessage(request: UploadJobRequest): Unit = {
    val jobId: String = request.getJobId
    logger.info(s"received upload request for job $jobId")

    val job: UploadJob = uploadJobRepository.findById(jobId).orElse(null)

    if (job == null) {
      logger.error(s"no upload job found for id $jobId, ignoring request")
    } else if (job.getStatus == UploadJob.STATUS_CANCELLING) {
      // Cancelled while still sitting in the queue, nothing was parsed yet
      finalizeCancelledBeforeParse(job)
    } else if (job.getStatus != UploadJob.STATUS_SCHEDULED) {
      // Idempotency guard: only a freshly SCHEDULED job is work to be done
      logger.warn(s"ignoring upload request for job $jobId in non-schedulable state ${job.getStatus}")
    } else {
      // Leave the job SCHEDULED and hand off to the background worker
      parseExecutor.submit(new Runnable {
        override def run(): Unit = processJob(jobId)
      })
    }
  }

  /** Finalizes a job that was cancelled before any parsing happened: nothing was persisted, so the
    * spectra count stays zero and the raw file is dropped */
  private def finalizeCancelledBeforeParse(job: UploadJob): Unit = {
    job.setStatus(UploadJob.STATUS_CANCELLED)
    job.setLastUpdated(new Date())
    uploadJobRepository.save(job)
    uploadStorageService.deleteJob(job.getId)
    logger.info(s"upload job ${job.getId} cancelled before parsing started")
  }

  /** Parses the assembled file and persists every spectrum, walking the job to a terminal status.
    * Runs on parseExecutor, decoupled from the AMQP delivery, driven entirely by the durable
    * UploadJob row so it is recoverable independently of the message */
  private def processJob(jobId: String): Unit = {
    val job: UploadJob = uploadJobRepository.findById(jobId).orElse(null)

    if (job == null) {
      logger.error(s"no upload job found for id $jobId when starting parse, ignoring")
    } else if (job.getStatus == UploadJob.STATUS_CANCELLING) {
      // A cancel request can land in the window between scheduling and the worker picking the job up
      finalizeCancelledBeforeParse(job)
    } else if (job.getStatus != UploadJob.STATUS_SCHEDULED) {
      // Dedup: a duplicate delivery (or a startup re-enqueue racing the original) already claimed or
      // finished this job 
      logger.warn(s"skipping parse for job $jobId already in state ${job.getStatus}")
    } else {
      var parsed = 0L
      var persisted = 0L
      var failed = 0L

      try {
        job.setStatus(UploadJob.STATUS_RUNNING)
        job.setLastUpdated(new Date())
        uploadJobRepository.save(job)

        // Admin has no submitter row, fabricate user
        val (submitterEmail, submitterFirstName, submitterLastName, submitterInstitution) =
          if (job.getEmailAddress == "admin") {
            ("admin", "admin", "", "")
          } else {
            val submitter = submitterRepository.findTopByEmailAddress(job.getEmailAddress)
            if (submitter == null) {
              throw new IllegalStateException(s"no submitter account found for ${job.getEmailAddress}")
            }
            (submitter.getEmailAddress, submitter.getFirstName, submitter.getLastName, submitter.getInstitution)
          }

        // The library form's submitter fields (optional) fully override the uploader's own identity for
        // every spectrum in the job
        val (effectiveSubmitterEmail, effectiveSubmitterFirstName, effectiveSubmitterLastName, effectiveSubmitterInstitution) =
          if (job.getLibrarySubmitterEmail != null && job.getLibrarySubmitterEmail.nonEmpty) {
            (job.getLibrarySubmitterEmail, job.getLibrarySubmitterFirstName, job.getLibrarySubmitterLastName, job.getLibrarySubmitterInstitution)
          } else {
            (submitterEmail, submitterFirstName, submitterLastName, submitterInstitution)
          }

        // Default to massbank.us for library link
        val libraryLink = Option(job.getLibraryLink).filter(_.nonEmpty).getOrElse("http://massbank.us")

        // Extra tags from the library form's additional tags input, parsed once and reused
        val additionalTags: List[String] =
          Option(job.getAdditionalTags).filter(_.nonEmpty)
            .map(json => objectMapper.readValue(json, classOf[Array[String]]).toList)
            .getOrElse(Nil)

        val reader: DomainReader[RawParsedSpectrum] = job.getFormat match {
          case "msp" => new MSPReader
          case "mgf" => new MGFReader
          case "massbank" => new MassBankReader
          case other => throw new IllegalStateException(s"unsupported upload format: $other")
        }

        job.setTotal(countTotal(job.getStoredPath, job.getFormat))
        uploadJobRepository.save(job)

        val fileReader = Files.newBufferedReader(Paths.get(job.getStoredPath), StandardCharsets.UTF_8)
        try {
          reader.read(fileReader, new DomainReadEventHandler[RawParsedSpectrum] {
            override def readEvent(raw: RawParsedSpectrum): Unit = {
              parsed += 1
              try {
                // A fresh SpectrumSubmitter (and, when a library was requested, a fresh Library)
                // per spectrum: both are JPA entities with a generated id, and Spectrum owns each
                // one exclusively (@OneToOne/@OneToMany cascade=ALL, orphanRemoval=true)
                val spectrumSubmitter = new SpectrumSubmitter(
                  effectiveSubmitterEmail, effectiveSubmitterFirstName, effectiveSubmitterLastName, effectiveSubmitterInstitution
                )
                val library = Option(job.getLibraryName).filter(_.nonEmpty).map { name =>
                  new Library(job.getLibraryDescription, libraryLink, name)
                }
                val id = Option(job.getLibraryPrefix).filter(_.nonEmpty).map { prefix =>
                  f"$prefix${libraryPrefixCounterService.nextValue(prefix)}%06d"
                }
                val spectrum = UploadSpectrumBuilder.build(raw, job.getFileName, spectrumSubmitter, id, additionalTags, library)
                spectrumPersistenceService.save(spectrum)
                persisted += 1
              } catch {
                case e: Exception =>
                  logger.warn(s"upload job $jobId: failed to persist one spectrum: ${e.getMessage}")
                  failed += 1
              }

              if (parsed % ProgressFlushBatchSize == 0) {
                val fresh: UploadJob = uploadJobRepository.findById(jobId).orElse(job)
                if (fresh.getStatus == UploadJob.STATUS_CANCELLING) {
                  throw new UploadCancelledException
                }
                fresh.setParsed(parsed)
                fresh.setPersisted(persisted)
                fresh.setFailed(failed)
                fresh.setLastUpdated(new Date())
                uploadJobRepository.save(fresh)
              }
            }
          })
        } finally {
          fileReader.close()
        }

        job.setParsed(parsed)
        job.setPersisted(persisted)
        job.setFailed(failed)
        job.setStatus(UploadJob.STATUS_COMPLETE)
        job.setLastUpdated(new Date())
        uploadJobRepository.save(job)

        uploadStorageService.deleteJob(jobId)
        logger.info(s"upload job $jobId complete: $persisted persisted, $failed failed out of $parsed parsed")
      } catch {
        case _: UploadCancelledException =>
          // Spectra persisted so far are kept, only the remainder of the file is abandoned
          job.setParsed(parsed)
          job.setPersisted(persisted)
          job.setFailed(failed)
          job.setStatus(UploadJob.STATUS_CANCELLED)
          job.setLastUpdated(new Date())
          uploadJobRepository.save(job)
          uploadStorageService.deleteJob(jobId)
          logger.info(s"upload job $jobId cancelled: $persisted persisted out of $parsed parsed before cancel")

        case e: Exception =>
          logger.error(s"upload job $jobId failed: ${e.getMessage}", e)
          job.setStatus(UploadJob.STATUS_FAILED)
          job.setErrorMessage(e.getMessage)
          job.setLastUpdated(new Date())
          uploadJobRepository.save(job)
          uploadStorageService.deleteJob(jobId)
      }
    }
  }

  // Cheap first pass so the progress bar has a real denominator before the main parse loop starts
  private def countTotal(storedPath: String, format: String): Long = format match {
    case "msp" => countOccurrences(storedPath, "Num Peaks")
    case "mgf" => countOccurrences(storedPath, "BEGIN IONS")
    case "massbank" =>
      val count = countOccurrences(storedPath, "PK$NUM_PEAK")
      if (count > 1) 0L else count
    case _ => 0L
  }

  /** Streams the file in bounded chunks rather than loading it whole, carrying a marker sized
    * tail across chunk boundaries so an occurrence split across two chunks is still counted */
  private def countOccurrences(path: String, marker: String): Long = {
    val reader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)
    try {
      val buf = new Array[Char](1024 * 1024)
      var carryTail = ""
      var count = 0L
      var n = reader.read(buf)
      while (n != -1) {
        val text = carryTail + new String(buf, 0, n)
        var idx = text.indexOf(marker)
        while (idx != -1) {
          count += 1
          idx = text.indexOf(marker, idx + 1)
        }
        carryTail = if (text.length >= marker.length) text.substring(text.length - marker.length + 1) else text
        n = reader.read(buf)
      }
      count
    } finally {
      reader.close()
    }
  }
}
