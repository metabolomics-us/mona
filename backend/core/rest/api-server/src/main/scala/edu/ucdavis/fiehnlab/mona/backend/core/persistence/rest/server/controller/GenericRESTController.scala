package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller

/**
  * Created by wohlgemuth on 3/9/16.
  */

import java.util.concurrent.Future
import javax.servlet.{ServletRequest, ServletResponse}
import javax.validation.Valid

import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._

import scala.collection.JavaConverters._

/**
  * This is the main spectrum related REST controller and should be utilized for most operations
  * interacting with the system.
  *
  * It allows you to perform the standard operations, you would like todo on a spectrum
  *
  */
abstract class GenericRESTController[T] {

  /**
    * utilized repository
    *
    * @return
    */
  def getRepository: PagingAndSortingRepository[T, String]

  /**
    * this will return all the specified data in the system
    * please be aware that this can cause out of memory issues
    * and should be always utilized with pagination
    *
    * @return
    */

  @RequestMapping(path = Array(""), method = Array(RequestMethod.GET),produces = Array("application/json","text/msp"))
  @Async
  @ResponseBody
  def list(@RequestParam(value = "page", required = false) page: Integer, @RequestParam(value = "size", required = false) size: Integer ,servletRequest: ServletRequest,servletResponse: ServletResponse ): Future[ResponseEntity[Iterable[T]]] = {

    val data: Iterable[T] = {
      if (size != null) {
        if (page != null) {
          getRepository.findAll(new PageRequest(page, size)).getContent.asScala
        }
        else {
          getRepository.findAll(new PageRequest(0, size)).getContent.asScala
        }
      }
      else {
        getRepository.findAll().asScala
      }
    }

    val headers = new HttpHeaders()
  //  headers.add("Content-Type",servletRequest.getContentType)

    val entity = new ResponseEntity(data, headers,HttpStatus.OK)

    new AsyncResult[ResponseEntity[Iterable[T]]](
      entity
    )
  }


  /**
    * this method returns the complete count of spectra in the system
    *
    * @return
    */
  @RequestMapping(path = Array("/count"), method = Array(RequestMethod.GET))
  @Async
  @ResponseBody
  def searchCount: Future[Long] = {
    new AsyncResult[Long](getRepository.count())
  }


  /**
    * saves a spectra or updates it. This will depend on the utilized repository
    *
    * @param spectrum
    * @return
    */
  @Async
  @RequestMapping(path = Array(""), method = Array(RequestMethod.POST))
  @ResponseBody
  def save(@RequestBody @Valid spectrum: T) = new AsyncResult[T](
    getRepository.save(spectrum)
  )

  /**
    * looks for the exact spectra
    *
    * @param spectrum
    * @return
    */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.GET),produces = Array("application/json","text/msp"))
  @ResponseBody
  def get(@PathVariable("id") spectrum: String,servletRequest: ServletRequest,servletResponse: ServletResponse): Future[ResponseEntity[T]] = {
    val headers = new HttpHeaders()
  //  headers.add("Content-Type",servletRequest.getContentType)

    if (getRepository.exists(spectrum)) {
      new AsyncResult[ResponseEntity[T]](
        new ResponseEntity[T](getRepository.findOne(spectrum),headers, HttpStatus.OK)

      )
    }
    else {
      new AsyncResult[ResponseEntity[T]](
        new ResponseEntity[T](HttpStatus.NOT_FOUND)
      )
    }
  }


  /**
    * this methods removes the specified method from the system
    *
    * @param spectrum
    * @return
    */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.DELETE))
  @ResponseBody
  def delete(@PathVariable("id") spectrum: String) = getRepository.delete(spectrum)


  /**
    * saves the provided spectrum at the given path
    *
    * @param id
    * @param spectrum
    * @return
    */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.PUT))
  @ResponseBody
  def put(@PathVariable("id") id: String, @Valid @RequestBody spectrum: T): Future[ResponseEntity[T]] = {
    new AsyncResult[ResponseEntity[T]](
      new ResponseEntity(getRepository.save(spectrum), HttpStatus.OK)
    )
  }
}