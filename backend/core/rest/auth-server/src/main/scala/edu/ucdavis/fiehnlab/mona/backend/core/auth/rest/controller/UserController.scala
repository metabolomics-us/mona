package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

import java.util.Collections
import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginInfo
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._

import scala.collection.JavaConverters
import JavaConverters._

/**
  * Created by wohlgemuth on 4/4/16.
  */
@CrossOrigin
@RestController
@RequestMapping(value = Array("/rest/users"))
class UserController extends GenericRESTController[User] {

  @Autowired
  val httpServletRequest: HttpServletRequest = null

  @Autowired
  val userRepository: UserRepository = null

  @Autowired
  val loginService: LoginService = null

  /**
    * Saves a user or updates it
    *
    * @param user
    * @return
    */
  override def doSave(user: User): Future[ResponseEntity[User]] = {
    // Users cannot update existing accounts
    val existingUser: User = userRepository.findOne(user.username)

    if (existingUser == null) {
      super.doSave(user.copy(roles = Collections.emptyList()))
    } else {
      new AsyncResult[ResponseEntity[User]](new ResponseEntity[User](HttpStatus.CONFLICT))
    }
  }

  /**
    * Saves the provided user at the given path
    *
    * @param id
    * @param user
    * @return
    */
  override def doPut(id: String, user: User): Future[ResponseEntity[User]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if (loginInfo.roles.contains("ADMIN")) {
      // Admins can update any user
      super.doPut(id, user)
    } else {
      // Users can only update their own accounts
      val existingUser: User = userRepository.findOne(id)

      if (loginInfo.username == existingUser.username) {
        super.doPut(id, user.copy(roles = Collections.emptyList()))
      } else {
        new AsyncResult[ResponseEntity[User]](new ResponseEntity[User](HttpStatus.FORBIDDEN))
      }
    }
  }

  /**
    * utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[User, String] = userRepository
}
