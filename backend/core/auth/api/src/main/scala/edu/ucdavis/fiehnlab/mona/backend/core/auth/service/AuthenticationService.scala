package edu.ucdavis.fiehnlab.mona.backend.core.auth.service

import org.springframework.security.core.Authentication

/**
  * a simple service to provide us with a authentication based
  * on a token ß
  */
trait AuthenticationService {

  def authenticate(token:String) : Authentication


}
