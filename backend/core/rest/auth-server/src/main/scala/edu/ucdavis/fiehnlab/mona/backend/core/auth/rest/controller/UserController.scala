package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

import java.util.Collections
import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.ResponseEntity
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
    * saves the provided spectrum at the given path
    *
    * @param id
    * @param user
    * @return
    */
  override def put(@PathVariable("id") id: String, @Validated @RequestBody user: User): Future[ResponseEntity[User]] = {
    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last

    println("YAYAYA")
    println(token)
    println(loginService.info(token).roles.asScala.mkString(", "))

    if (loginService.info(token).roles.contains("ADMIN")) {
      super.put(id, user)
    } else {
      super.put(id, user.copy(roles = Collections.emptyList()))
    }
  }

  /**
    * utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[User, String] = userRepository
}
