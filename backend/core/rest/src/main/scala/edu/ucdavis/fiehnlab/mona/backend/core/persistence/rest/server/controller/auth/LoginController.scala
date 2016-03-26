package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.auth

import java.util.Date

import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginRequest, LoginResponse}
import io.jsonwebtoken.{Jwts, SignatureAlgorithm}
import org.apache.commons.lang.time.DateUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{RequestBody, RequestMapping, RequestMethod, RestController}

import scala.collection.JavaConverters._

/**
  * JWT based token authentification for our mona based services
  */
@RestController
@RequestMapping(value = Array("/rest/auth"))
class LoginController {

  /**
    * time how long a token is valid for in hours
    */
  val timeOfLife = 24

  /**
    * the secret key used for encoding our keys
    */
  @Autowired
  val tokenSecret: TokenSecret = null

  @Autowired
  val loginService: LoginService = null

  @RequestMapping(path = Array("/login"), method = Array(RequestMethod.POST))
  def login(@RequestBody request: LoginRequest): LoginResponse = {

    val user = loginService.login(request)

    val issueDate = new Date()
    val experiationDate = DateUtils.addHours(issueDate, timeOfLife)

    //associated roles
    val roles = user.roles.asScala.collect {
      case x: Role => x.name
    }.asJava

    new LoginResponse(Jwts.builder().setSubject(user.username).claim("roles", roles).setIssuedAt(issueDate).setExpiration(experiationDate).signWith(SignatureAlgorithm.HS256, tokenSecret.value).compact())

  }

}
