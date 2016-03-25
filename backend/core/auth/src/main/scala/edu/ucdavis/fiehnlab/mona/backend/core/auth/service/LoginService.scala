package edu.ucdavis.fiehnlab.mona.backend.core.auth.service

import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{LoginRequest, User}

/**
  * a simple services doing acutal login for us
  */
trait LoginService {

  /**
    * does a login for the current user
    *
    * @return
    */
  def login(request: LoginRequest) : User
}
