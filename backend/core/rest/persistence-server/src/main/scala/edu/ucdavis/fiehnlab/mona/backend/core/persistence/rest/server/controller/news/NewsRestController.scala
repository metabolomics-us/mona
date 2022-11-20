
package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.news

import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.domain.News
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.NewsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.{Page, PageRequest, Pageable, Sort}
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._

import scala.jdk.CollectionConverters._
import java.util.concurrent.Future
import javax.servlet.{ServletRequest, ServletResponse}
import javax.validation.Valid

/**
  * Created by sajjan on 04/05/17.
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest/news"))
@Profile(Array("mona.persistence"))
class NewsRestController{

  @Autowired
  val newsRepository: NewsRepository = null

  val fetchSize: Int = 50

  /**
   * Returns all the specified data in the system.  Should be utilized with pagination to avoid
   * out of memory issues
   *
   * @return
   */
  @RequestMapping(path = Array(""), method = Array(RequestMethod.GET), produces = Array("application/json", "text/msp", "text/sdf", "image/png"))
  @Async
  @ResponseBody
  final def list(@RequestParam(value = "page", required = false) page: Integer, @RequestParam(value = "size", required = false) size: Integer): Future[ResponseEntity[Iterable[News]]] = {
    doList(page, size)
  }

  def doList(page: Integer, size: Integer): Future[ResponseEntity[Iterable[News]]] = {
    val data: Iterable[News] = {
      if (size != null) {
        if (page != null) {
          newsRepository.findAll(PageRequest.of(page, size, Sort.Direction.ASC, "id")).getContent.asScala
        } else {
          newsRepository.findAll(PageRequest.of(0, size, Sort.Direction.ASC, "id")).getContent.asScala
        }
      } else {
        new DynamicIterable[News, String]("", fetchSize) {
          // loads more data from the server for the given query
          override def fetchMoreData(query: String, pageable: Pageable): Page[News] = newsRepository.findAll(pageable)
        }.asScala
      }
    }

    val headers = new HttpHeaders()
    // headers.add("Content-Type", servletRequest.getContentType)

    new AsyncResult[ResponseEntity[Iterable[News]]](
      new ResponseEntity(data, headers, HttpStatus.OK)
    )
  }


  /**
   * Returns the complete count of resources in the system
   *
   * @return
   */
  @RequestMapping(path = Array("/count"), method = Array(RequestMethod.GET))
  @Async
  @ResponseBody
  final def searchCount: Future[Long] = {
    new AsyncResult[Long](newsRepository.count())
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
  final def save(@Valid @RequestBody resource: News): Future[ResponseEntity[News]] = doSave(resource)

  def doSave(resource: News): Future[ResponseEntity[News]] = {
    new AsyncResult[ResponseEntity[News]](
      new ResponseEntity[News](newsRepository.save(resource), HttpStatus.OK)
    )
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
  final def get(@PathVariable("id") id: Long, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[News]] = {
    doGet(id, servletRequest, servletResponse)
  }

  def doGet(id: Long, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[News]] = {
    val headers = new HttpHeaders()
    // headers.add("Content-Type", servletRequest.getContentType)

    if (newsRepository.existsById(id)) {
      new AsyncResult[ResponseEntity[News]](new ResponseEntity[News](newsRepository.findById(id).orElse(null), headers, HttpStatus.OK))
    } else {
      new AsyncResult[ResponseEntity[News]](new ResponseEntity[News](HttpStatus.NOT_FOUND))
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
  final def delete(@PathVariable("id") id: Long): Unit = doDelete(id)

  def doDelete(id: Long): Unit = newsRepository.deleteById(id)


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
  final def put(@PathVariable("id") id: Long, @Valid @RequestBody resource: News): Future[ResponseEntity[News]] = {
    doPut(id, resource)
  }

  def doPut(id: Long, resource: News): Future[ResponseEntity[News]] = {
    new AsyncResult[ResponseEntity[News]](new ResponseEntity(newsRepository.save(resource), HttpStatus.OK))
  }
}
