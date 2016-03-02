package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.client.{RestOperations, RestTemplate}

/**
  * * a generic approach to connect to a REST server and execute operations against it
  */
abstract class GenericRestClient[T,ID] (basePath:String) {

  @Autowired
  val restOperations:RestOperations = null

  /**
    * returns the count of the database content, which can be narrowed down by an optional quyery
    *
    * @param query
    * @return
    */
  def count(query:Option[String] = None) : Long = restOperations.getForEntity(s"${basePath}/count",classOf[Long]).

  /**
    * adds a type to the system
    *
    * @param dao
    * @return
    */
  def add(dao:T) : Option[T]

  /**
    * updates a type in the system
    *
    * @param dao
    * @return
    */
  def update(dao: T, id:ID) : Option[T]

  /**
    * deletes a type from the system
    *
    * @param dao
    */
  def delete(dao: T)

  /**
    * list data matching the optional conditions or returns all
    *
    * @param query
    * @param page
    * @param pageSize
    * @return
    */
  def list(query:Option[String] = None, page:Option[Int] = None, pageSize:Option[Int] = None) : List[T]

}
