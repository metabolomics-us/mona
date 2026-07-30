package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.advice

import com.turkraft.springfilter.exception.{InternalFilterException, SpringFilterException}
import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation.{ExceptionHandler, RestControllerAdvice}

import javax.servlet.http.HttpServletRequest

/**
  * Turns spring-filter failures into proper status codes instead of letting them escape the
  * controllers
  */
@RestControllerAdvice
class FilterExceptionHandler extends LazyLogging {

  @Autowired
  val httpServletRequest: HttpServletRequest = null

  /**
    * the offending url is the useful part of the log line, since it identifies the client or the
    * stale link that still sends the old syntax
    */
  private def describeRequest: String = {
    if (httpServletRequest == null) {
      "unknown request"
    } else if (httpServletRequest.getQueryString == null) {
      httpServletRequest.getRequestURI
    } else {
      s"${httpServletRequest.getRequestURI}?${httpServletRequest.getQueryString}"
    }
  }

  /**
    * bad syntax, unknown functions and unauthorized paths all originate in the submitted query, so
    * they are client errors. The stack trace adds nothing over the message, so only the request and
    * the reason are logged
    */
  @ExceptionHandler(Array(classOf[SpringFilterException]))
  def handleBadFilter(e: SpringFilterException): ResponseEntity[java.util.Map[String, String]] = {
    logger.warn(s"rejecting malformed filter query on $describeRequest: ${e.getMessage}")

    val body: java.util.Map[String, String] = java.util.Map.of(
      "error", s"invalid query syntax: ${e.getMessage}",
      "hint", "queries use spring-filter syntax, for example exists(metaData.name:'ionization mode' and metaData.value:'positive'). The older RSQL form using == and =q= is no longer supported"
    )

    new ResponseEntity(body, HttpStatus.BAD_REQUEST)
  }

  /**
    * an internal filter failure is a fault on our side rather than a bad query, so it keeps its 500
    * and its stack trace. Handling it still beats letting it escape, since the escaped version
    * reaches the caller as an empty 401
    */
  @ExceptionHandler(Array(classOf[InternalFilterException]))
  def handleInternalFilterFailure(e: InternalFilterException): ResponseEntity[java.util.Map[String, String]] = {
    logger.error(s"filter evaluation failed internally on $describeRequest: ${e.getMessage}", e)

    val body: java.util.Map[String, String] = java.util.Map.of("error", "the query could not be evaluated")

    new ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR)
  }
}
