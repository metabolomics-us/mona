package edu.ucdavis.fiehnlab.mona.backend.core.auth.service

import org.springframework.security.config.annotation.web.builders.HttpSecurity

/**
  * a simple helper interface to allow easier configuration of the security context of rest servicesß
  */
trait RestSecurityService {

  /**
    * prepares the http security object for our use
    *
    * @param httpSecurity
    * @return
    */
  def prepare(httpSecurity: HttpSecurity) : HttpSecurity
}
