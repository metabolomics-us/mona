package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{MediaType, HttpEntity, HttpHeaders}
import org.springframework.web.client.{RestOperations, RestTemplate}

import scala.reflect.ClassTag
import scala.reflect._


/**
  * a generic approach to connect to a REST server and execute operations against it. It assumes compliance with CRUD operations
  */
class GenericRestClient[T: ClassTag, ID](basePath: String) {

  @Autowired
  val restOperations: RestOperations = null

  /**
    * returns the count of the database content, which can be narrowed down by an optional quyery
    *
    * @param query
    * @return
    */
  def count(query: Option[String] = None): Long = query match {
    case Some(x) =>

      val headers = new HttpHeaders()
      headers.setContentType(MediaType.APPLICATION_JSON);

      val entity = new HttpEntity[String](x,headers)

      restOperations.postForObject(s"$basePath/count", entity, classOf[Long])

    case _ => restOperations.getForObject(s"$basePath/count", classOf[Long])
  }

  /**
    * adds a type to the system
    *
    * @param dao
    * @return
    */
  def add(dao: T): T = restOperations.postForObject(basePath, dao, classTag[T].runtimeClass).asInstanceOf[T]

  /**
    * updates a type in the system
    *
    * @param dao
    * @return
    */
  def update(dao: T, id: ID): T = {
    restOperations.put(s"${basePath}/${id}", dao)
    get(id)
  }

  /**
    * deletes the give object id from the system
    *
    * @param id
    */
  def delete(id: ID) = restOperations.delete(s"$basePath/$id")

  /**
    * loads the object specified by the id
    *
    * @param id
    * @return
    */
  def get(id: ID): T = restOperations.getForObject(s"$basePath/$id", classTag[T].runtimeClass).asInstanceOf[T]

  /**
    * list data matching the optional conditions or returns all
    *
    * @param query    optional query to execute against the rest service
    * @param page     optional page to goto
    * @param pageSize optional page size
    * @return
    */
  def list(query: Option[String] = None, page: Option[Int] = None, pageSize: Option[Int] = None): Array[T] = {


    val utilizedPageSize: String = pageSize match {
      case Some(a) => s"?size=$a"
      case _ => ""
    }

    val pageToLookAt: String = page match {
      case Some(a) => utilizedPageSize match {
        case "" => s"?page=$a"
        case _ => s"&page=$a"
      }
      case _ => ""
    }

    val path = query match {
      case Some(a) => s"$basePath/query$utilizedPageSize$pageToLookAt"
      case _ => s"$basePath$utilizedPageSize$pageToLookAt"
    }

    if (query.isEmpty) {
      restOperations.getForObject(path, classTag[Array[T]].runtimeClass).asInstanceOf[Array[T]]
    }
    else {
      restOperations.postForObject(path, query, classTag[Array[T]].runtimeClass).asInstanceOf[Array[T]]
    }
  }

}
