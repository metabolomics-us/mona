package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.submitter

import com.typesafe.scalalogging.LazyLogging

import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest
import javax.servlet.{ServletRequest, ServletResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginInfo
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Submitter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SubmitterRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.{Page, PageRequest, Pageable, Sort}
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._

import javax.validation.Valid
import scala.jdk.CollectionConverters._

/**
  * Created by wohlgemuth on 3/7/16.
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest/submitters"))
@Profile(Array("mona.persistence"))
class SubmitterRestController extends LazyLogging{

  @Autowired
  val httpServletRequest: HttpServletRequest = null

  @Autowired
  val loginService: LoginService = null

  var fetchSize: Int = 50

  @RequestMapping(path = Array(""), method = Array(RequestMethod.GET), produces = Array("application/json", "text/msp", "text/sdf", "image/png"))
  @Async
  @ResponseBody
  final def list(@RequestParam(value = "page", required = false) page: Integer, @RequestParam(value = "size", required = false) size: Integer): Future[ResponseEntity[Iterable[Submitter]]] = {
    doList(page, size)
  }

  def adminDoList(page: Integer, size: Integer): Future[ResponseEntity[Iterable[Submitter]]] = {
    val data: Iterable[Submitter] = {
      if (size != null) {
        if (page != null) {
          submitterRepository.findAll(PageRequest.of(page, size, Sort.Direction.ASC, "id")).getContent.asScala
        } else {
          submitterRepository.findAll(PageRequest.of(0, size, Sort.Direction.ASC, "id")).getContent.asScala
        }
      } else {
        new DynamicIterable[Submitter, String]("", fetchSize) {
          // loads more data from the server for the given query
          override def fetchMoreData(query: String, pageable: Pageable): Page[Submitter] = submitterRepository.findAll(pageable)
        }.asScala
      }
    }

    val headers = new HttpHeaders()
    // headers.add("Content-Type", servletRequest.getContentType)

    new AsyncResult[ResponseEntity[Iterable[Submitter]]](
      new ResponseEntity(data, headers, HttpStatus.OK)
    )
  }

  /**
    * Returns all the specified data in the system.
    *
    * @return
    */
  def doList(page: Integer, size: Integer): Future[ResponseEntity[Iterable[Submitter]]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if (loginInfo.roles.contains("ADMIN")) {
      adminDoList(page, size)
    } else if (submitterRepository.existsByEmailAddress(loginInfo.emailAddress)) {
      new AsyncResult[ResponseEntity[Iterable[Submitter]]](
        new ResponseEntity[Iterable[Submitter]](Array(submitterRepository.findByEmailAddress(loginInfo.emailAddress)), HttpStatus.OK)
      )
    } else {
      new AsyncResult[ResponseEntity[Iterable[Submitter]]](new ResponseEntity[Iterable[Submitter]](Array.empty[Submitter], HttpStatus.OK))
    }
  }

  /**
   * Returns the specified resource
   *
   * @param id
   * @return
   */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.GET), produces = Array("application/json", "text/msp", "text/sdf", "image/png"))
  @ResponseBody
  final def get(@PathVariable("id") id: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[Submitter]] = {
    doGet(id, servletRequest, servletResponse)
  }

  def adminDoGet(id: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[Submitter]] = {
    val headers = new HttpHeaders()
    // headers.add("Content-Type", servletRequest.getContentType)

    if (submitterRepository.existsByEmailAddress(id)) {
      new AsyncResult[ResponseEntity[Submitter]](new ResponseEntity[Submitter](submitterRepository.findByEmailAddress(id), headers, HttpStatus.OK))
    } else {
      new AsyncResult[ResponseEntity[Submitter]](new ResponseEntity[Submitter](HttpStatus.NOT_FOUND))
    }
  }
  /**
    * Returns the specified submitter
    *
    * @param id
    * @return
    */
  def doGet(id: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[Submitter]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if (loginInfo.roles.contains("ADMIN") || id == loginInfo.emailAddress) {
      adminDoGet(id, servletRequest, servletResponse)
    } else {
      new AsyncResult[ResponseEntity[Submitter]](new ResponseEntity[Submitter](HttpStatus.FORBIDDEN))
    }
  }


  /**
   * Saves a resource or updates it
   *
   * @param resource
   * @return
   */
  @Async
  @RequestMapping(path = Array(""), method = Array(RequestMethod.POST))
  @ResponseBody
  final def save(@Valid @RequestBody resource: Submitter): Future[ResponseEntity[Submitter]] = doSave(resource)

  def adminDoSave(resource: Submitter): Future[ResponseEntity[Submitter]] = {
    new AsyncResult[ResponseEntity[Submitter]](
      new ResponseEntity[Submitter](submitterRepository.save(resource), HttpStatus.OK)
    )
  }
  /**
    * Saves a submitter or updates it.  This will depend on the utilized repository
    *
    * @param submitter
    * @return
    */
  def doSave(submitter: Submitter): Future[ResponseEntity[Submitter]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    logger.info(s"${loginInfo}")
    if (loginInfo.roles.contains("ADMIN")) {
      adminDoSave(submitter)
    } else {
      val existingUser: Submitter = submitterRepository.findByEmailAddress(loginInfo.emailAddress)

      if (existingUser == null || existingUser.getEmailAddress == loginInfo.emailAddress) {
        adminDoSave(submitter)
      } else {
        new AsyncResult[ResponseEntity[Submitter]](new ResponseEntity[Submitter](HttpStatus.CONFLICT))
      }
    }
  }

  /**
   * Saves the provided resource at the given path
   *
   * @param id
   * @param resource
   * @return
   */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.PUT))
  @ResponseBody
  final def put(@PathVariable("id") id: String, @Valid @RequestBody resource: Submitter): Future[ResponseEntity[Submitter]] = {
    doPut(id, resource)
  }

  def adminDoPut(id: String, resource: Submitter): Future[ResponseEntity[Submitter]] = {
    new AsyncResult[ResponseEntity[Submitter]](new ResponseEntity(submitterRepository.save(resource), HttpStatus.OK))
  }
  /**
    * Saves the provided submitter at the given path
    *
    * @param id
    * @param submitter
    * @return
    */
  def doPut(id: String, submitter: Submitter): Future[ResponseEntity[Submitter]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if (loginInfo.roles.contains("ADMIN") || id == loginInfo.emailAddress) {
      adminDoPut(id, submitter)
    } else {
      new AsyncResult[ResponseEntity[Submitter]](new ResponseEntity[Submitter](HttpStatus.FORBIDDEN))
    }
  }

  /**
   * Removes the specified resource from the system
   *
   * @param id
   * @return
   */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.DELETE))
  @ResponseBody
  final def delete(@PathVariable("id") id: String): Unit = doDelete(id)

  def doDelete(id: String): Unit = submitterRepository.deleteByEmailAddress(id)

  /**
   * Returns the complete count of resources in the system
   *
   * @return
   */
  @RequestMapping(path = Array("/count"), method = Array(RequestMethod.GET))
  @Async
  @ResponseBody
  final def searchCount: Future[Long] = {
    new AsyncResult[Long](submitterRepository.count())
  }

  /**
    * this is the utilized repository, doing all the heavy lifting
    */
  @Autowired
  val submitterRepository: SubmitterRepository = null
}
