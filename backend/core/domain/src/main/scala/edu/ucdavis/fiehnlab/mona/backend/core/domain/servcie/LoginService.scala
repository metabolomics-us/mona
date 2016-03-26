package edu.ucdavis.fiehnlab.mona.backend.core.domain.servcie

import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginRequest, LoginResponse}

/**
  * defines some way for us to login to a system
  */
trait LoginService {

  /**
    * does a login for the current user
    * and returns a response or throws a related exception
    *
    * @return
    */
  def login(request: LoginRequest) : LoginResponse

}

