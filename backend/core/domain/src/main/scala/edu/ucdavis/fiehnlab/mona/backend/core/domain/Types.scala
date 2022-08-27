package edu.ucdavis.fiehnlab.mona.backend.core.domain

import java.util.Date

/**
  * makes serializations and authorizations simpler
  */
object HelperTypes {

  /**
    * a simple query
    *
    * @param string
    */
  case class WrappedString(string: String)


  /**
    * a login token
    */
  case class LoginResponse(token: String)

  /**
    * a login request
    *
    * @param emailAddress
    * @param password
    */
  case class LoginRequest(emailAddress: String, password: String)

  /**
    * general information about a token to be retrieved from the server and can be useful for client side applications
    *
    * @param emailAddress
    * @param validFrom
    * @param validTo
    * @param roles
    */
  case class LoginInfo(emailAddress: String, validFrom: Date, validTo: Date, roles: java.util.List[String])

}