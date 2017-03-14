package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller

/**
  * Created by wohlgemuth on 3/9/16.
  */

import java.util.concurrent.Future
import javax.servlet.{ServletRequest, ServletResponse}
import javax.validation.Valid

import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import org.springframework.data.domain.{Page, PageRequest, Pageable, Sort}
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._

import scala.collection.JavaConverters._

/**
  * This is the main abstract REST controller and should be utilized for most operations
  * interacting with the system.
  *
  * It allows you to perform the standard operations you would like to do on any resource
  */
abstract class GenericRESTController[T] {

  var fetchSize: Int = 50

  /**
    * Utilized repository
    *
    * @return
    */
  def getRepository: PagingAndSortingRepository[T, String]


  /**
    * Returns all the specified data in the system.  Should be utilized with pagination to avoid
    * out of memory issues
    *
    * @return
    */
  @RequestMapping(path = Array(""), method = Array(RequestMethod.GET), produces = Array("application/json", "text/msp"))
  @Async
  @ResponseBody
  final def list(@RequestParam(value = "page", required = false) page: Integer, @RequestParam(value = "size", required = false) size: Integer): Future[ResponseEntity[Iterable[T]]] = {
    doList(page, size)
  }

  def doList(page: Integer, size: Integer): Future[ResponseEntity[Iterable[T]]] = {
    val data: Iterable[T] = {
      if (size != null) {
        if (page != null) {
          getRepository.findAll(new PageRequest(page, size, Sort.Direction.ASC, "id")).getContent.asScala
        } else {
          getRepository.findAll(new PageRequest(0, size, Sort.Direction.ASC, "id")).getContent.asScala
        }
      } else {
        new DynamicIterable[T, String]("", fetchSize) {
          // loads more data from the server for the given query
          override def fetchMoreData(query: String, pageable: Pageable): Page[T] = getRepository.findAll(pageable)
        }.asScala
      }
    }

    val headers = new HttpHeaders()
    // headers.add("Content-Type", servletRequest.getContentType)

    new AsyncResult[ResponseEntity[Iterable[T]]](
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
    new AsyncResult[Long](getRepository.count())
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
  final def save(@Valid @RequestBody resource: T): Future[ResponseEntity[T]] = doSave(resource)

  def doSave(resource: T): Future[ResponseEntity[T]] = {
    new AsyncResult[ResponseEntity[T]](
      new ResponseEntity[T](getRepository.save(resource), HttpStatus.OK)
    )
  }


  /**
    * Returns the specified resource
    *
    * @param id
    * @return
    */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.GET), produces = Array("application/json", "text/msp"))
  @ResponseBody
  final def get(@PathVariable("id") id: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[T]] = {
    doGet(id, servletRequest, servletResponse)
  }

  def doGet(id: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[T]] = {
    val headers = new HttpHeaders()
    // headers.add("Content-Type", servletRequest.getContentType)

    if (getRepository.exists(id)) {
      new AsyncResult[ResponseEntity[T]](new ResponseEntity[T](getRepository.findOne(id), headers, HttpStatus.OK))
    } else {
      new AsyncResult[ResponseEntity[T]](new ResponseEntity[T](HttpStatus.NOT_FOUND))
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

  def doDelete(id: String): Unit = getRepository.delete(id)


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
  final def put(@PathVariable("id") id: String, @Valid @RequestBody resource: T): Future[ResponseEntity[T]] = {
    doPut(id, resource)
  }

  def doPut(id: String, resource: T): Future[ResponseEntity[T]] = {
    new AsyncResult[ResponseEntity[T]](
      new ResponseEntity(getRepository.save(resource), HttpStatus.OK)
    )
  }
}