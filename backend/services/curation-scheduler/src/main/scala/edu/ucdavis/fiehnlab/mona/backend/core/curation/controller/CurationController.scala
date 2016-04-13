package edu.ucdavis.fiehnlab.mona.backend.core.curation.controller

import org.springframework.scheduling.annotation.Async
import org.springframework.web.bind.annotation.{RequestParam, PathVariable, RequestMapping, RestController}

/**
  * This controller permitts us to easily schedule the curation of spectra in the system
  * these calls will be internally forwarded to a queue, which will take care of the actual
  * execution. This allows the service to be very cheap and to easily scale
  */
@RestController
@RequestMapping(value = Array("/rest/curation"))
class CurationController {

  @RequestMapping(path = Array("/{id}"))
  @Async
  def curateById(@PathVariable("id") id: String) = {

  }

  @RequestMapping(path = Array("/"))
  @Async
  def curateByQuery(@RequestParam(required = true, name = "query") query: String) = {

  }

  @RequestMapping(path = Array("/"))
  @Async
  def curateAll = {

  }
}
