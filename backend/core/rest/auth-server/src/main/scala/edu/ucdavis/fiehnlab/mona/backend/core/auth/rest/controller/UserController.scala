package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

import java.util.Collections
import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginInfo
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.security.core.context.SecurityContextHolder
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
    * saves a spectra or updates it. This will depend on the utilized repository
    *
    * @param user
    * @return
    */
  @Async
  @RequestMapping(path = Array(""), method = Array(RequestMethod.POST))
  @ResponseBody
  override def save(@RequestBody user: User): AsyncResult[User] = {
    super.save(user.copy(roles = Collections.emptyList()))
  }

  /**
    * saves the provided user at the given path
    *
    * @param id
    * @param user
    * @return
    */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.PUT))
  @ResponseBody
  override def put(@PathVariable("id") id: String, @Validated @RequestBody user: User): Future[ResponseEntity[User]] = {
    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if (loginInfo.roles.contains("ADMIN")) {
      // Admins can update any user
      super.put(id, user)
    } else {
      // Users can only update their own accounts
      val existingUser: User = userRepository.findOne(id)

      if (loginInfo.username == existingUser.username) {
        super.put(id, user.copy(roles = Collections.emptyList()))
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
