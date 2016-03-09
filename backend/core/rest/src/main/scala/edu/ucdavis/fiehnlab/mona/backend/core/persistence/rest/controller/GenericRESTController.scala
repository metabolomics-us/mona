package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller

/**
  * Created by wohlgemuth on 3/9/16.
  */

import java.util.concurrent.Future

import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{AsyncResult, Async}
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
  @RequestMapping(path = Array(""), method = Array(RequestMethod.GET))
  @Async
  def list(@RequestParam(value = "page", required = false) page: Integer, @RequestParam(value = "size", required = false) size: Integer): Future[Iterable[T]] = new AsyncResult[Iterable[T]](
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
  )


  /**
    * this method returns the complete count of spectra in the system
    *
    * @return
    */
  @RequestMapping(path = Array("/count"), method = Array(RequestMethod.GET))
  @Async
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
  def save(@RequestBody spectrum: T) = new AsyncResult[T](
    getRepository.save(spectrum)
  )

  /**
    * looks for the exact spectra
    *
    * @param spectrum
    * @return
    */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.GET))
  def get(@PathVariable("id") spectrum: String): Future[ResponseEntity[T]] = {

    if (getRepository.exists(spectrum)) {
      new AsyncResult[ResponseEntity[T]](
        new ResponseEntity[T](getRepository.findOne(spectrum), HttpStatus.OK)
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
  def delete(@PathVariable("id") spectrum: String) = getRepository.delete(spectrum)

}