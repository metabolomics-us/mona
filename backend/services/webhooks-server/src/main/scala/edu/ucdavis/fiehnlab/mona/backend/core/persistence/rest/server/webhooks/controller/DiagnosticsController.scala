package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.service.{CloudWatchLogsService, LogQueryResult}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation._

import scala.jdk.CollectionConverters._

/**
  * lets admins see recent error log lines for each microservice, pulled from CloudWatch Logs
  */
@RestController
@RequestMapping(Array("/rest/diagnostics"))
class DiagnosticsController extends LazyLogging {

  @Autowired
  val cloudWatchLogsService: CloudWatchLogsService = null

  /**
    * lists the known service keys, so the frontend can build a selector
    */
  @RequestMapping(path = Array("/services"), method = Array(RequestMethod.GET))
  def services(): java.util.Map[String, String] = cloudWatchLogsService.serviceStreams.asJava

  /**
    * returns recent ERROR-level log lines for the given service
    *
    * @param service the service key, as returned by [[services]]
    * @param hours   how far back to look, defaults to 24
    */
  @RequestMapping(path = Array("/logs"), method = Array(RequestMethod.GET))
  def logs(@RequestParam("service") service: String,
           @RequestParam(name = "hours", defaultValue = "24") hours: Int): ResponseEntity[LogQueryResult] = {
    cloudWatchLogsService.serviceStreams.get(service) match {
      case Some(streamName) =>
        val since = System.currentTimeMillis() - hours * 3600000L
        new ResponseEntity(cloudWatchLogsService.fetchErrorLogs(streamName, since), HttpStatus.OK)
      case None =>
        logger.warn(s"diagnostics requested for unknown service key: $service")
        new ResponseEntity(HttpStatus.BAD_REQUEST)
    }
  }

  /**
    * best-effort fetch of the stack trace/continuation lines following a single error entry,
    * identified by its timestamp as returned by [[logs]]
    */
  @RequestMapping(path = Array("/logs/trace"), method = Array(RequestMethod.GET))
  def trace(@RequestParam("service") service: String,
            @RequestParam("timestamp") timestamp: Long): ResponseEntity[String] = {
    cloudWatchLogsService.serviceStreams.get(service) match {
      case Some(streamName) =>
        new ResponseEntity(cloudWatchLogsService.fetchLogContext(streamName, timestamp), HttpStatus.OK)
      case None =>
        logger.warn(s"diagnostics trace requested for unknown service key: $service")
        new ResponseEntity(HttpStatus.BAD_REQUEST)
    }
  }
}
