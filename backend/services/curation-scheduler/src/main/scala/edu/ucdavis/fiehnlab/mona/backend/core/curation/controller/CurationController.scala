package edu.ucdavis.fiehnlab.mona.backend.core.curation.controller

import java.util.concurrent.atomic.AtomicBoolean
import javax.servlet.http.HttpServletRequest
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.curation.service.{CurationRunner, CurationService}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation._


/**
  * This controller permits us to easily schedule the curation of spectra in the system
  * these calls will be internally forwarded to a queue, which will take care of the actual
  * execution. This allows the service to be very cheap and to easily scale
  */
@RestController
@RequestMapping(value = Array("/rest/curation"))
class CurationController extends LazyLogging {
  @Autowired
  val curationService: CurationService = null

  @Autowired
  val curationRunner: CurationRunner = null

  @Autowired
  val rabbitAdmin: RabbitAdmin = null

  @Autowired
  @Qualifier("spectra-curation-queue")
  val queueName: String = null

  // Only tracks the enqueue phase, not the curation itself
  private val schedulingInProgress: AtomicBoolean = new AtomicBoolean(false)

  // Reject a fresh mass curation while this many spectra are still queued from a previous run
  @Value("${mona.curation.mass-backlog-threshold:1000}")
  val massCurationBacklogThreshold: Int = 1000

  /**
    * schedules the spectra with the specified id for curation
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/{id}"))
  def curateById(@PathVariable("id") id: String, request: HttpServletRequest): ResponseEntity[CurationJobScheduled] = {
    val spectrum = curationService.curateById(id)

    if (spectrum == null) {
      new ResponseEntity(HttpStatus.NOT_FOUND)
    } else {
      new ResponseEntity[CurationJobScheduled](CurationJobScheduled(1), HttpStatus.OK)
    }
  }

  /**
    * schedules every spectra for the given query for curation
    *
    * @param query
    */
  @RequestMapping(path = Array(""))
  def curateByQuery(@RequestParam(required = false, name = "query") query: String): ResponseEntity[String] = {

    // Reject if a mass curation is already scheduling
    if (!schedulingInProgress.compareAndSet(false, true)) {
      return new ResponseEntity[String]("Re-curation scheduling already in progress", HttpStatus.CONFLICT)
    }

    // Reject if a previous batch is still draining through the queue so we don't pile on a second batch
    val backlog: Int = Option(rabbitAdmin.getQueueInfo(queueName)).map(_.getMessageCount).getOrElse(0)

    if (backlog > massCurationBacklogThreshold) {
      schedulingInProgress.set(false)
      return new ResponseEntity[String](s"Re-curation already in progress, $backlog spectra still queued", HttpStatus.CONFLICT)
    }

    // Delegated to an @Async runner bean so the request returns immediately
    // The runner clears schedulingInProgress in a finally block when scheduling completes
    curationRunner.scheduleAllForCuration(query, schedulingInProgress)
    new ResponseEntity[String]("Re-curation scheduled for all spectra", HttpStatus.ACCEPTED)
  }

  /**
    * Curate a single spectrum on demand
    */
  @RequestMapping(path = Array(""), method = Array(RequestMethod.POST))
  def curateSpectrum(@RequestBody spectrum: Spectrum): ResponseEntity[Spectrum] = {
    new ResponseEntity(curationService.curateSpectrum(spectrum), HttpStatus.OK)
  }
}

@Schema
case class CurationJobScheduled(count: Int)
