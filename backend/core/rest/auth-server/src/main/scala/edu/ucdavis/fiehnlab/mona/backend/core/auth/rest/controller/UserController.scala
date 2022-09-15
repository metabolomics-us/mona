package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

import com.typesafe.scalalogging.LazyLogging

import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Roles, Users}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginInfo
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, PageRequest, Pageable, Sort}
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._

import javax.servlet.{ServletRequest, ServletResponse}
import javax.validation.Valid
import scala.jdk.CollectionConverters._

/**
  * Created by wohlgemuth on 4/4/16.
  */
@CrossOrigin
@RestController
@RequestMapping(value = Array("/rest/users"))
class UserController extends LazyLogging{

  @Autowired
  val httpServletRequest: HttpServletRequest = null

  @Autowired
  val userRepository: UserRepository = null

  @Autowired
  val loginService: LoginService = null

  var fetchSize: Int = 50


  /**
   * Returns all the specified data in the system.  Should be utilized with pagination to avoid
   * out of memory issues
   *
   * @return
   */
  @RequestMapping(path = Array(""), method = Array(RequestMethod.GET), produces = Array("application/json", "text/msp", "text/sdf", "image/png"))
  @Async
  @ResponseBody
  final def list(@RequestParam(value = "page", required = false) page: Integer, @RequestParam(value = "size", required = false) size: Integer): Future[ResponseEntity[Iterable[Users]]] = {
    doList(page, size)
  }

  def doList(page: Integer, size: Integer): Future[ResponseEntity[Iterable[Users]]] = {
    val data: Iterable[Users] = {
      if (size != null) {
        if (page != null) {
          userRepository.findAll(PageRequest.of(page, size, Sort.Direction.ASC, "id")).getContent.asScala
        } else {
          userRepository.findAll(PageRequest.of(0, size, Sort.Direction.ASC, "id")).getContent.asScala
        }
      } else {
        new DynamicIterable[Users, String]("", fetchSize) {
          // loads more data from the server for the given query
          override def fetchMoreData(query: String, pageable: Pageable): Page[Users] = userRepository.findAll(pageable)
        }.asScala
      }
    }

    val headers = new HttpHeaders()
    // headers.add("Content-Type", servletRequest.getContentType)

    new AsyncResult[ResponseEntity[Iterable[Users]]](
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
    new AsyncResult[Long](userRepository.count())
  }

  /**
   * Returns the specified resource
   *
   * @param id
   * @return
   */
  @Async
  @RequestMapping(path = Array("/{emailAddress}"), method = Array(RequestMethod.GET), produces = Array("application/json", "text/msp", "text/sdf", "image/png"))
  @ResponseBody
  final def get(@PathVariable("emailAddress") emailAddress: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[Users]] = {
    doGet(emailAddress, servletRequest, servletResponse)
  }

  def doGet(emailAddress: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[Users]] = {
    val headers = new HttpHeaders()
    // headers.add("Content-Type", servletRequest.getContentType)

    if (userRepository.existsByEmailAddress(emailAddress)) {
      new AsyncResult[ResponseEntity[Users]](new ResponseEntity[Users](userRepository.findByEmailAddress(emailAddress), headers, HttpStatus.OK))
    } else {
      new AsyncResult[ResponseEntity[Users]](new ResponseEntity[Users](HttpStatus.NOT_FOUND))
    }
  }

  /**
    * Saves a user or updates it
    *
    * @param user
    * @return
    */
  def doSave(user: Users): Future[ResponseEntity[Users]] = {
    // Users cannot update existing accounts
    val existingUser: Users = userRepository.findByEmailAddress(user.getEmailAddress)

    if (existingUser == null) {
      user.setRoles(List(new Roles()).asJava)
      new AsyncResult[ResponseEntity[Users]](
        new ResponseEntity[Users](userRepository.save(user), HttpStatus.OK)
      )
    } else {
      new AsyncResult[ResponseEntity[Users]](new ResponseEntity[Users](HttpStatus.CONFLICT))
    }
  }

  @Async
  @RequestMapping(path = Array(""), method = Array(RequestMethod.POST))
  @ResponseBody
  final def save(@Valid @RequestBody resource: Users): Future[ResponseEntity[Users]] = doSave(resource)

  /**
    * Saves the provided user at the given path
    *
    * @param id
    * @param user
    * @return
    */
  def doPut(emailAddress: String, user: Users): Future[ResponseEntity[Users]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if (loginInfo.roles.contains("ADMIN")) {
      // Admins can update any user
      new AsyncResult[ResponseEntity[Users]](new ResponseEntity(userRepository.save(user), HttpStatus.OK))
    } else {
      // Users can only update their own accounts
      val existingUser: Users = userRepository.findByEmailAddress(emailAddress)

      if (loginInfo.emailAddress == existingUser.getEmailAddress) {
        user.setRoles(List(new Roles).asJava)
        new AsyncResult[ResponseEntity[Users]](new ResponseEntity(userRepository.save(user), HttpStatus.OK))
      } else {
        new AsyncResult[ResponseEntity[Users]](new ResponseEntity[Users](HttpStatus.FORBIDDEN))
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
  @RequestMapping(path = Array("/{emailAddress}"), method = Array(RequestMethod.PUT))
  @ResponseBody
  final def put(@PathVariable("emailAddress") emailAddress: String, @Valid @RequestBody resource: Users): Future[ResponseEntity[Users]] = {
    doPut(emailAddress, resource)
  }

  /**
   * Removes the specified resource from the system
   *
   * @param emailAddress
   * @return
   */
  @Async
  @RequestMapping(path = Array("/{emailAddress}"), method = Array(RequestMethod.DELETE))
  @ResponseBody
  final def delete(@PathVariable("emailAddress") emailAddress: String): Unit = doDelete(emailAddress)

  def doDelete(emailAddress: String): Unit = userRepository.deleteByEmailAddress(emailAddress)
}
