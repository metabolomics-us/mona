package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

/**
  * Created by wohlg on 3/27/2016.
  */


import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.User
import edu.ucdavis.fiehnlab.mona.backend.core.domain.servcie.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginRequest, LoginResponse}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.web.bind.annotation.{RequestBody, RequestMapping, RequestMethod, RestController}

/**
  * simple controller which allows us to authenticate against
  * a provided login delegate.
  *
  * please make sure that your local security config allows POST access to this service at any given time without authentication or it will fail
  */
@RestController
@RequestMapping(value = Array("/rest/auth"))
class LoginController extends LazyLogging {

  @Autowired
  @Qualifier("loginServiceDelegate")
  val loginService: LoginService = null

  @RequestMapping(path = Array("/login"), method = Array(RequestMethod.POST))
  def login(@RequestBody request: LoginRequest): LoginResponse = {
    logger.debug(s"forwarding authentication request to: ${loginService}")
    loginService.login(request)
  }

}