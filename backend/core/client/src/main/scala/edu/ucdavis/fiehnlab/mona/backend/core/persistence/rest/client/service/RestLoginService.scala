package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.service

import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginRequest, LoginResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.servcie.LoginService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.client.RestOperations

/**
  * this is a simple rest based implementation of our login service
  */
class RestLoginService(val remoteServer:String, val remotePort:Int, val protocol:String = "http") extends LoginService{

  @Autowired
  val restOperations:RestOperations = null

  /**
    * does a login for the current user
    * and returns a response or throws a related exception
    *
    * @return
    */
  override def login(request: LoginRequest): LoginResponse = restOperations.postForEntity(s"${protocol}://${remoteServer}:${remotePort}/rest/auth/login",request,classOf[LoginResponse]).getBody
}
