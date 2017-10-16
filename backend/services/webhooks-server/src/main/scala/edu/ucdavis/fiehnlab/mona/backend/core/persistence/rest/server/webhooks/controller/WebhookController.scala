package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller

import java.util.concurrent.Future
import javax.servlet.{ServletRequest, ServletResponse}
import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginInfo
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository.WebHookRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.service.WebHookService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.{WebHook, WebHookResult}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._

/**
  * this is used to synchronize several webhooks with each other
  */
@RestController
@RequestMapping(Array("/rest/webhooks"))
class WebhookController extends GenericRESTController[WebHook] with LazyLogging {

  @Autowired
  val httpServletRequest: HttpServletRequest = null

  @Autowired
  val loginService: LoginService = null

  @Autowired
  val webhookRepository: WebHookRepository = null

  @Autowired
  val webHookService: WebHookService = null

  /**
    * utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[WebHook, String] = webhookRepository


  /**
    * Returns all the specified data in the system.
    *
    * @return
    */
  override def doList(page: Integer, size: Integer): Future[ResponseEntity[Iterable[WebHook]]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if(loginInfo.roles.contains("ADMIN")) {
      super.doList(page, size)
    } else {
      new AsyncResult[ResponseEntity[Iterable[WebHook]]](
        new ResponseEntity(webhookRepository.findByUsername(loginInfo.username), HttpStatus.OK)
      )
    }
  }

  /**
    * Returns the specified resource
    *
    * @param id
    * @return
    */
  override def doGet(id: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[WebHook]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if(loginInfo.roles.contains("ADMIN")) {
      super.doGet(id, servletRequest, servletResponse)
    } else if (webhookRepository.exists(id)) {
      val existingWebHook: WebHook = webhookRepository.findOne(id)

      if (existingWebHook.username == loginInfo.username) {
        new AsyncResult[ResponseEntity[WebHook]](new ResponseEntity[WebHook](existingWebHook, HttpStatus.OK))
      } else {
        new AsyncResult[ResponseEntity[WebHook]](new ResponseEntity[WebHook](HttpStatus.FORBIDDEN))
      }
    } else {
      new AsyncResult[ResponseEntity[WebHook]](new ResponseEntity[WebHook](HttpStatus.NOT_FOUND))
    }
  }


  /**
    * Saves a webhook or updates it. This will depend on the utilized repository
    *
    * @param webHook
    * @return
    */
  override def doSave(webHook: WebHook): Future[ResponseEntity[WebHook]] = {
    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if(loginInfo.roles.contains("ADMIN")) {
      super.doSave(webHook)
    } else if (webhookRepository.exists(webHook.name)) {
      val existingWebHook: WebHook = webhookRepository.findOne(webHook.name)

      if (existingWebHook.username == loginInfo.username) {
        super.doSave(webHook.copy(username = loginInfo.username))
      } else {
        new AsyncResult[ResponseEntity[WebHook]](new ResponseEntity[WebHook](HttpStatus.FORBIDDEN))
      }
    } else {
      super.doSave(webHook.copy(username = loginInfo.username))
    }
  }

  /**
    * saves the provided webhook at the given path
    *
    * @param id
    * @param webHook
    * @return
    */
  override def doPut(id: String, webHook: WebHook): Future[ResponseEntity[WebHook]] = {
    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if (loginInfo.roles.contains("ADMIN")) {
      super.doPut(id, webHook)
    } else if (webhookRepository.exists(webHook.name)) {
      val existingWebHook: WebHook = webhookRepository.findOne(webHook.name)

      if (existingWebHook.username == loginInfo.username) {
        super.doPut(id, webHook)
      } else {
        new AsyncResult[ResponseEntity[WebHook]](new ResponseEntity[WebHook](HttpStatus.FORBIDDEN))
      }
    } else {
      super.doPut(id, webHook)
    }
  }


  /**
    *
    * @param id
    */
  @RequestMapping(path = Array("/trigger/{id}/{type}"), method = Array(RequestMethod.POST))
  def triggerHooksForSpectrumId(@PathVariable("id") id: String, @PathVariable("type") eventType: String): Future[Array[WebHookResult]] = {

    logger.info(s"triggering event hooks for spectra: $id and type $eventType")

    // trigger all webhooks
    new AsyncResult[Array[WebHookResult]](webHookService.trigger(id, eventType))
  }


  /**
    * pulls all data from the master
    * @param query
    * @return
    */
  @RequestMapping(path = Array("/pull"), method = Array(RequestMethod.POST))
  def pull(@RequestParam(name = "query", required = false, defaultValue = "") query: String): Unit = {
    if (query == "") {
      webHookService.pull()
    } else {
      webHookService.pull(Some(query))
    }
  }


  /**
    * pushes all local data, to all it's slaves
    * @param query
    * @return
    */
  @RequestMapping(path = Array("/push"), method = Array(RequestMethod.POST))
  def push(@RequestParam(name = "query", required = false, defaultValue = "") query: String): Unit = {
    if (query == "") {
      webHookService.push()
    } else {
      webHookService.push(Some(query))
    }
  }

  /**
    * provides a webhook client from mona to a master mona instance
    *
    * @param id
    * @param eventType
    */
  @RequestMapping(path = Array("/sync"), method = Array(RequestMethod.GET))
  @Async
  def sync(@RequestParam("id") id: String, @RequestParam("type") eventType: String): ResponseEntity[Any] = {
    webHookService.sync(id, eventType)
  }
}
