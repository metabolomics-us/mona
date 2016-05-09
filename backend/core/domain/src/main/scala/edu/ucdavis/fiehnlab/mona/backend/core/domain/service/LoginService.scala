package edu.ucdavis.fiehnlab.mona.backend.core.domain.service

import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginInfo, LoginRequest, LoginResponse}

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

  /**
    * does a login for the current user
    * and returns a response or throws a related exception
    *
    * @return
    */
  def login(username: String, password: String): LoginResponse = login(LoginRequest(username, password))

  /**
    * generates publicly interesting info about the given token
    * @param token
    * @return
    */
  def info(token:String) : LoginInfo

  /**
    * etends the given token, to create a token which doesn't expire
    * @param token
    * @return
    */
  def extend(token:String) : LoginResponse
}

