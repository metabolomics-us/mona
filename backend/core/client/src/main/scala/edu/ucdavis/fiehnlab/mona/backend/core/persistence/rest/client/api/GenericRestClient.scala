package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api

import java.util
import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginRequest
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.{Page, PageImpl, Pageable}
import org.springframework.http._
import org.springframework.web.client.RestOperations

import scala.collection.JavaConverters._
import scala.reflect.{ClassTag, _}


/**
  * a generic approach to connect to a REST server and execute operations against it. It assumes compliance with CRUD operations
  */
class GenericRestClient[T: ClassTag, ID](basePath: String) extends LazyLogging {

  /**
    * internal private
    * authorization token
    * which should be kept secret
    */
  private var token: String = _

  @Autowired
  @Qualifier("monaRestServer")
  val monaRestServer: String = null

  @Autowired
  val loginService: LoginService = null

  @Autowired
  protected val restOperations: RestOperations = null

  //build our request path on the fly for us
  def requestPath = s"$monaRestServer/$basePath"

  @PostConstruct
  def init(): Unit = {
    logger.info(s"utilizing base path for queries: $requestPath, host is $monaRestServer")
  }

  /**
    * returns the count of the database content, which can be narrowed down by an optional quyery
    *
    * @param query
    * @return
    */
  def count(query: Option[String] = None): Long = query match {
    case Some(x) =>
      restOperations.getForObject(s"$requestPath/search/count?query={query}", classOf[Long], x)

    case _ => restOperations.getForObject(s"$requestPath/count", classOf[Long])
  }

  /**
    * adds a type to the system
    *
    * @param dao
    * @return
    */
  def add(dao: T): T = {
    restOperations.exchange(s"$requestPath", HttpMethod.POST, new HttpEntity[T](dao, buildHeaders), classTag[T].runtimeClass).getBody.asInstanceOf[T]
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
  def updateAsync(dao: T, id: ID): Unit = {
    restOperations.exchange(s"$requestPath/$id", HttpMethod.PUT, new HttpEntity[T](dao, buildHeaders), classTag[T].runtimeClass)
  }


  /**
    * updates a type in the system
    *
    * @param dao
    * @return
    */
  def update(dao: T, id: ID): T = {
    restOperations.exchange(s"$requestPath/$id", HttpMethod.PUT, new HttpEntity[T](dao, buildHeaders), classTag[T].runtimeClass)
    get(id)
  }

  /**
    * deletes the give object id from the system
    *
    * @param id
    */
  def delete(id: ID): Unit = {
    restOperations.exchange(s"$requestPath/$id", HttpMethod.DELETE, new HttpEntity[Nothing](buildHeaders), classTag[T].runtimeClass)
  }

  /**
    * loads the object specified by the id
    *
    * @param id
    * @return
    */
  def get(id: ID): T = {
    restOperations.exchange(s"$requestPath/$id", HttpMethod.GET, new HttpEntity[T](buildHeaders), classTag[T].runtimeClass).getBody.asInstanceOf[T]
  }

  /**
    * streams the results to the client
    *
    * @param query
    * @return
    */
  def stream(query: Option[String], fetchSize: Option[Int] = Some(10)): Iterable[T] = {
    new DynamicIterable[T, Option[String]](query, fetchSize.get) {

      /**
        * required for the pagination
        */
      val internalCount: Long = count(query)

      logger.info(s"total count of data to receive: $internalCount")

      /**
        * loads more data from the server for the given query
        */
      override def fetchMoreData(query: Option[String], pageable: Pageable): Page[T] = {
        val params: String = s"?size=${pageable.getPageSize}&page=${pageable.getPageNumber}"

        val result: Array[T] = query match {
          case Some(x) =>
            logger.debug(s"calling path: $requestPath/search$params&query=$x")
            restOperations.getForObject(s"$requestPath/search$params&query={query}", classTag[Array[T]].runtimeClass, x).asInstanceOf[Array[T]]
          case _ =>
            logger.debug(s"calling path: $requestPath$params")
            restOperations.getForObject(s"$requestPath$params", classTag[Array[T]].runtimeClass).asInstanceOf[Array[T]]
        }

        logger.debug(s"received: ${result.length}, page: ${pageable.getPageNumber} size: ${pageable.getPageSize}")
        result.foreach { s => logger.debug(s"\tspectra: $s") }

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
    val sizeParam: String = pageSize match {
      case Some(a) => s"?size=$a"
      case _ => ""
    }

    val pageParam: String = page match {
      case Some(a) => sizeParam match {
        case "" => s"?page=$a"
        case _ => s"&page=$a"
      }
      case _ => ""
    }

    query match {
      case Some(x) =>
        val path: String = s"$requestPath/search$sizeParam$pageParam"

        val queryParam = pageParam match {
          case "" => "?query="
          case _ => "&query="
        }

        logger.info(s"path to invoke: $path$queryParam=$x")
        restOperations.getForObject(s"$path$queryParam{query}", classTag[Array[T]].runtimeClass, x).asInstanceOf[Array[T]]

      case _ =>
        val path: String = s"$requestPath$sizeParam$pageParam"

        logger.debug(s"path to invoke: $path")
        restOperations.getForObject(path, classTag[Array[T]].runtimeClass).asInstanceOf[Array[T]]
    }
  }

  /**
    * executes a login against the provided login server
    *
    * @param username
    * @param password
    */
  final def login(username: String, password: String): GenericRestClient[T, ID] = {
    logger.debug(s"logging in using service: $loginService")
    val token = loginService.login(LoginRequest(username, password)).token
    login(token)
  }

  /**
    * logs in with the provided token
    *
    * @param token
    */
  final def login(token: String): GenericRestClient[T, ID] = {
    logger.debug("utilizing token for authorization in this instance")
    this.token = token
    this
  }

  /**
    * builds our headers for authorization
    *
    * @return
    */
  protected def buildHeaders: HttpHeaders = {
    val header = new HttpHeaders()
    header.set("Authorization", s"Bearer $token")
    header.setAccept(util.Arrays.asList(MediaType.APPLICATION_JSON))
    header
  }
}
