package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.config

import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod, RestController}

@RestController
@RequestMapping(Array(""))
class TestController {

  @RequestMapping(path = Array("/info"), method = Array(RequestMethod.GET))
  def test(): ResponseEntity[Any] = new ResponseEntity[Any](HttpStatus.OK)
}
