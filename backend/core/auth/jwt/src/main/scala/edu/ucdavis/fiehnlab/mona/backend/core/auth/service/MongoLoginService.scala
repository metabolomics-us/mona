package edu.ucdavis.fiehnlab.mona.backend.core.auth.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginRequest, LoginResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.servcie.LoginService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException

/**
  * simple login implementation utilizing mongo as a service
  */
class MongoLoginService extends LoginService with LazyLogging{

  @Autowired
  val userRepository: UserRepository = null

  @Autowired
  val tokenService:TokenService = null

  /**
    * does a login and returns a token for us, which can be used internally
    * or externally
    *
    * @return
    */
  override def login(request: LoginRequest): LoginResponse = {

    logger.debug(s"login in ${request.username}")

    val user: User = userRepository.findByUsername(request.username)

    if (user == null) {
      throw new UsernameNotFoundException(s"sorry user ${request.username} was not found")
    }
    else if (user.password != request.password) {
      throw new BadCredentialsException("sorry the provided credentials were invalid!")
    }
    else {
      logger.debug("login was successful")
      LoginResponse(tokenService.generateToken(user))
    }
  }
}
