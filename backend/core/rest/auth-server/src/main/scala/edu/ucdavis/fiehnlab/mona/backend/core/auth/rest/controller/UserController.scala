package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.{Roles, Users}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginInfo
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.web.bind.annotation._
import scala.jdk.CollectionConverters._

/**
  * Created by wohlgemuth on 4/4/16.
  */
@CrossOrigin
@RestController
@RequestMapping(value = Array("/rest/users"))
class UserController extends GenericRESTController[Users] {

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
  override def doSave(user: Users): Future[ResponseEntity[Users]] = {
    // Users cannot update existing accounts
    val existingUser: Users = userRepository.findByUsername(user.getEmailAddress)

    if (existingUser == null) {
      user.setRoles(List(new Roles()).asJava)
      super.doSave(user)
    } else {
      new AsyncResult[ResponseEntity[Users]](new ResponseEntity[Users](HttpStatus.CONFLICT))
    }
  }

  /**
    * Saves the provided user at the given path
    *
    * @param id
    * @param user
    * @return
    */
  override def doPut(id: String, user: Users): Future[ResponseEntity[Users]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if (loginInfo.roles.contains("ADMIN")) {
      // Admins can update any user
      super.doPut(id, user)
    } else {
      // Users can only update their own accounts
      val existingUser: Users = userRepository.findById(id).get()

      if (loginInfo.username == existingUser.getEmailAddress) {
        user.setRoles(List(new Roles).asJava)
        super.doSave(user)
      } else {
        new AsyncResult[ResponseEntity[Users]](new ResponseEntity[Users](HttpStatus.FORBIDDEN))
      }
    }
  }

  /**
    * utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[Users, String] = userRepository
}
