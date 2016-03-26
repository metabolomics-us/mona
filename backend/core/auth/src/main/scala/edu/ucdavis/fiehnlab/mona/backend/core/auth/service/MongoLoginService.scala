package edu.ucdavis.fiehnlab.mona.backend.core.auth.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException

/**
  * simple login implementation utilizing mongo as a service
  */
class MongoLoginService extends LoginService with LazyLogging{

  @Autowired
  val userRepository: UserRepository = null

  /**
    * does a login for the current user
    *
    * @return
    */
  override def login(request: LoginRequest): User = {

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
      user
    }
  }
}
