package edu.ucdavis.fiehnlab.mona.backend.core.auth.service

import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginInfo




/**
  * a simple service to provide us with tokens for usersß
  */
trait TokenService {
  /**
    * provide us with some information about this token
    * @param token
    * @return
    */
  def info(token: String): LoginInfo


  /**
    * generates a token for us
    * based on the given user
    *
    * @param user
    * @return
    */
  def generateToken(user:User) : String
}