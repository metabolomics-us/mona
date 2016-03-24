package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api

import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.WrappedString
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.{Page, PageImpl, Pageable}
import org.springframework.web.client.RestOperations

import scala.collection.JavaConverters._
import scala.reflect.{ClassTag, _}


/**
  * a generic approach to connect to a REST server and execute operations against it. It assumes compliance with CRUD operations
  */
class GenericRestClient[T: ClassTag, ID](basePath: String) extends LazyLogging {

  @Autowired
  @Qualifier("monaRestServer")
  val monaRestServer: String = null

  @Autowired
  protected val restOperations: RestOperations = null

  //build our request path on the fly for us
  def requestPath = s"$monaRestServer/$basePath"

  @PostConstruct
  def init = {
    logger.info(s"utilizing base path for queries: ${requestPath}")

  }

  /**
    * returns the count of the database content, which can be narrowed down by an optional quyery
    *
    * @param query
    * @return
    */
  def count(query: Option[String] = None): Long = query match {
    case Some(x) =>

      restOperations.postForObject(s"$requestPath/count", WrappedString(x), classOf[Long])

    case _ => restOperations.getForObject(s"$requestPath/count", classOf[Long])
  }

  /**
    * adds a type to the system
    *
    * @param dao
    * @return
    */
  def add(dao: T): T = {
    restOperations.postForObject(s"$requestPath", dao, classTag[T].runtimeClass).asInstanceOf[T]
  }

  /**
    * adds on object to service in a concurrent fashion
    * actual implementation will decided how to handle this
    *
    * @param dao
    */
  def addAsync(dao: T): Unit = add(dao)

  /**
    * updates the object in a concurrent session
    * actual implementation will decide how to handle this
    *
    * @param dao
    * @param id
    */
  def updateAsync(dao: T, id: ID): Unit = update(dao, id)

  /**
    * updates a type in the system
    *
    * @param dao
    * @return
    */
  def update(dao: T, id: ID): T = {
    restOperations.put(s"$requestPath/${id}", dao)
    get(id)
  }

  /**
    * deletes the give object id from the system
    *
    * @param id
    */
  def delete(id: ID) = restOperations.delete(s"$requestPath/$id")

  /**
    * loads the object specified by the id
    *
    * @param id
    * @return
    */
  def get(id: ID): T = restOperations.getForObject(s"$requestPath/$id", classTag[T].runtimeClass).asInstanceOf[T]

  /**
    * streams the results to the client
    * @param query
    * @return
    */
  def stream(query: Option[String], fetchSize:Option[Int] = Some(10)) : Iterable[T] = {
    new DynamicIterable[T, Option[String]](query, fetchSize.get) {

      /**
        * required for the pagination
        */
      val internalCount = count(query)

      /**
        * loads more data from the server for the given query
        */
      override def fetchMoreData(query: Option[String], pageable: Pageable): Page[T] = {

        var path: String = s"${requestPath}"

        query match {
          case Some(x) =>
            path = s"${path}/search?size=${fetchSize}&page=${pageable.getPageNumber}&query=${x}"
          case _ =>
            path = s"${path}?size=${fetchSize}&page=${pageable.getPageNumber}"
        }

        logger.debug(s"calling path: ${path}")
        val result = restOperations.getForObject(path, classTag[Array[T]].runtimeClass).asInstanceOf[Array[T]]

        logger.debug(s"received: ${result}")
        new PageImpl[T](result.toList.asJava, pageable, internalCount)
      }
    }.asScala

  }
  /**
    * list data matching the optional conditions or returns all
    *
    * @param query    optional query to execute against the rest service
    * @param page     optional page to goto
    * @param pageSize optional page size
    * @return
    */
  def list(query: Option[String] = None, page: Option[Int] = None, pageSize: Option[Int] = None): Iterable[T] = {

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

    val pathToInvoke = query match {
      case Some(a) =>
        val path = s"$requestPath/search$utilizedPageSize$pageToLookAt"

        if (path.contains("?")) {
          s"$path&query=${a}"
        }
        else {
          s"$path?query=${a}"
        }
      case _ =>
        s"$requestPath$utilizedPageSize$pageToLookAt"
    }
    restOperations.getForObject(pathToInvoke, classTag[Array[T]].runtimeClass).asInstanceOf[Array[T]]
  }

}
