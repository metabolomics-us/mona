package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.TokenService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginInfo, LoginRequest, LoginResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

/**
  * Simple login implementation utilizing mongo as a service
  */
class MongoLoginService extends LoginService with LazyLogging {

  @Autowired
  val userRepository: UserRepository = null

  @Autowired
  val tokenService: TokenService = null

  /**
    * Performs a login and returns a token for us, which can be used internally
    * or externally
    *
    * @return
    */
  override def login(request: LoginRequest): LoginResponse = {

    logger.debug(s"login in ${request.username}")

    val user: User = userRepository.findByUsername(request.username)

    if (user == null) {
      throw new UsernameNotFoundException(s"sorry user ${request.username} was not found")
    } else if (!new BCryptPasswordEncoder().matches(request.password, user.password)) {
      throw new BadCredentialsException("sorry the provided credentials were invalid!")
    } else {
      logger.debug("login was successful")
      LoginResponse(tokenService.generateToken(user))
    }
  }

  /**
    * Generates publicly interesting info about the given token
    *
    * @param token
    * @return
    */
  override def info(token: String): LoginInfo = tokenService.info(token)

  /**
    * Extends the given token, to create a token which doesn't expire for ten years
    *
    * @param token
    * @return
    */
  override def extend(token: String): LoginResponse = {
    val info = tokenService.info(token)

    LoginResponse(tokenService.generateToken(userRepository.findByUsername(info.username), 24 * 365 * 10))
  }
}
