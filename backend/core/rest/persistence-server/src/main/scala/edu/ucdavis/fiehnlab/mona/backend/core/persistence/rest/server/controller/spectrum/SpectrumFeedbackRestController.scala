package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginInfo
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{SpectrumFeedback, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.{ISubmitterMongoRepository, SpectrumFeedbackMongoRepository}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, PageRequest, Pageable, Sort}
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._

import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest
import javax.servlet.{ServletRequest, ServletResponse}
import javax.validation.Valid
import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 06/19/18.
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest/feedback"))
class SpectrumFeedbackRestController extends LazyLogging{

  @Autowired
  val commentRepository: SpectrumFeedbackMongoRepository = null

  @Autowired
  val httpServletRequest: HttpServletRequest = null

  @Autowired
  val loginService: LoginService = null

  @Autowired
  val submitterMongoRepository: ISubmitterMongoRepository = null

  /**
   * Returns the specified submitter
   *
   * @param id
   * @return
   */


  @RequestMapping(path = Array("/{monaID}"), method = Array(RequestMethod.GET), produces = Array("application/json"))
  @Async
  @ResponseBody
  def searchByMonaID(@PathVariable("monaID") id: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[java.lang.Iterable[SpectrumFeedback]]] = {
    if (id != null) {
      new AsyncResult[ResponseEntity[java.lang.Iterable[SpectrumFeedback]]](
        new ResponseEntity(commentRepository.findByMonaID(id), HttpStatus.OK)
      )
    } else {
      new AsyncResult[ResponseEntity[java.lang.Iterable[SpectrumFeedback]]](new ResponseEntity(HttpStatus.BAD_REQUEST))
    }
  }

  /**
    * Utilized repository
    *
    * @return
    */
  def getRepository: PagingAndSortingRepository[SpectrumFeedback, String] = commentRepository

  @RequestMapping(path = Array(""), method = Array(RequestMethod.GET), produces = Array("application/json", "text/msp", "text/sdf", "image/png"))
  @Async
  @ResponseBody
  final def list(@RequestParam(value = "page", required = false) page: Integer, @RequestParam(value = "size", required = false) size: Integer): Future[ResponseEntity[Iterable[SpectrumFeedback]]] = {
    doList(page, size)
  }

  def doList(page: Integer, size: Integer): Future[ResponseEntity[Iterable[SpectrumFeedback]]] = {
    val data: Iterable[SpectrumFeedback] = {
      if (size != null) {
        if (page != null) {
          getRepository.findAll(new PageRequest(page, size, Sort.Direction.ASC, "id")).getContent.asScala
        } else {
          getRepository.findAll(new PageRequest(0, size, Sort.Direction.ASC, "id")).getContent.asScala
        }
      } else {
        new DynamicIterable[SpectrumFeedback, String]("", 50) {
          // loads more data from the server for the given query
          override def fetchMoreData(query: String, pageable: Pageable): Page[SpectrumFeedback] = getRepository.findAll(pageable)
        }.asScala
      }
    }

    val headers = new HttpHeaders()
    // headers.add("Content-Type", servletRequest.getContentType)

    new AsyncResult[ResponseEntity[Iterable[SpectrumFeedback]]](
      new ResponseEntity(data, headers, HttpStatus.OK)
    )
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
  final def save(@Valid @RequestBody resource: SpectrumFeedback): Future[ResponseEntity[SpectrumFeedback]] = doSave(resource)

  def doSave(resource: SpectrumFeedback): Future[ResponseEntity[SpectrumFeedback]] = {
    new AsyncResult[ResponseEntity[SpectrumFeedback]](
      new ResponseEntity[SpectrumFeedback](getRepository.save(resource), HttpStatus.OK)
    )
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

  def doDelete(id: String): Unit = getRepository.deleteById(id)


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
  final def put(@PathVariable("id") id: String, @Valid @RequestBody resource: SpectrumFeedback): Future[ResponseEntity[SpectrumFeedback]] = {
    doPut(id, resource)
  }

  def doPut(id: String, resource: SpectrumFeedback): Future[ResponseEntity[SpectrumFeedback]] = {
    new AsyncResult[ResponseEntity[SpectrumFeedback]](new ResponseEntity(getRepository.save(resource), HttpStatus.OK))
  }
}
