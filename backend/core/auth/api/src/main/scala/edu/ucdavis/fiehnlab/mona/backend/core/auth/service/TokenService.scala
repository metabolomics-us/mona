package edu.ucdavis.fiehnlab.mona.backend.core.auth.service

import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User




/**
  * a simple service to provide us with tokens for usersß
  */
trait TokenService {

  /**
    * generates a token for us
    * based on the given user
    * @param user
    * @return
    */
  def generateToken(user:User) : String
}