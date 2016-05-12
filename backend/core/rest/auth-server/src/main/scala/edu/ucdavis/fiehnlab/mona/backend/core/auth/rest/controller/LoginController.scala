package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

/**
  * Created by wohlg on 3/27/2016.
  */


import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginRequest, LoginResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.http.{ResponseEntity, HttpStatus}
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation._

/**
  * simple controller which allows us to authenticate against
  * a provided login delegate.
  *
  * please make sure that your local security config allows POST access to this service at any given time without authentication or it will fail
  */
@CrossOrigin
@RestController
@RequestMapping(value = Array("/rest/auth"))
class LoginController extends LazyLogging {

  @Autowired
  @Qualifier("loginServiceDelegate")
  val loginService: LoginService = null

  /**
    * preforms the actual login for us
    * throws InvalidUserNameOrPasswordExecption (401 UNAUTHORIZED) if username or password are incorrect
    *
    * @param request
    * @return
    */
  @RequestMapping(path = Array("/login"), method = Array(RequestMethod.POST))
  def login(@RequestBody request: LoginRequest): LoginResponse = {
    logger.debug(s"forwarding authentication request to: ${loginService}")
    try {
      loginService.login(request)
    } catch {
      case ex: UsernameNotFoundException => throw new InvalidUserNameOrPasswordExecption
      case ex: BadCredentialsException => throw new InvalidUserNameOrPasswordExecption
    }
  }

  /**
    * provides us with public info about this token
    *
    * @param request
    */
  @RequestMapping(path = Array("/info"), method = Array(RequestMethod.POST))
  def tokenInfo(@RequestBody request: LoginResponse) = {
    loginService.info(request.token)
  }

  @RequestMapping(path=Array("/extend"), method = Array(RequestMethod.POST))
  def extendToken(@RequestBody request:LoginResponse) : LoginResponse = {
    loginService.extend(request.token)
  }
}

/**
  * Responds with a 401 instead of an exception when login with wrong credentials
  */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Invalid username or password")
class InvalidUserNameOrPasswordExecption extends RuntimeException {}
