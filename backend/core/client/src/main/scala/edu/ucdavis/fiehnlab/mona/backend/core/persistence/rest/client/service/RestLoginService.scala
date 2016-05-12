package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.service

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginInfo, LoginRequest, LoginResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpEntity, HttpHeaders, HttpMethod, MediaType}
import org.springframework.web.client.RestOperations

import scala.reflect._

/**
  * this is a simple rest based implementation of our login service
  */
class RestLoginService(val remoteServer:String, val remotePort:Int, val protocol:String = "http") extends LoginService with LazyLogging{

  @Autowired
  val restOperations:RestOperations = null

  /**
    * does a login for the current user
    * and returns a response or throws a related exception
    *
    * @return
    */
  override def login(request: LoginRequest): LoginResponse = restOperations.postForEntity(s"${protocol}://${remoteServer}:${remotePort}/rest/auth/login",request,classOf[LoginResponse]).getBody

  /**
    * generates publicly interesting info about the given token
    *
    * @param token
    * @return
    */
  override def info(token: String): LoginInfo = {
    val header = new HttpHeaders()
    header.set("Authorization", s"Bearer $token")
    header.setAccept(util.Arrays.asList(MediaType.APPLICATION_JSON))

    val url = s"${protocol}://${remoteServer}:${remotePort}/rest/auth/info"
    logger.info(s"invoking url: ${url}")

    restOperations.exchange(url,HttpMethod.POST,new HttpEntity[LoginResponse](new LoginResponse(token),header),classOf[LoginInfo]).getBody
  }

  /**
    * etends the given token, to create a token which doesn't expire
    *
    * @param token
    * @return
    */
  override def extend(token: String): LoginResponse = {
    val header = new HttpHeaders()
    header.set("Authorization", s"Bearer $token")
    header.setAccept(util.Arrays.asList(MediaType.APPLICATION_JSON))

    val url = s"${protocol}://${remoteServer}:${remotePort}/rest/auth/extend"
    logger.info(s"invoking url: ${url}")
    restOperations.exchange(url, HttpMethod.POST, new HttpEntity[LoginResponse](new LoginResponse(token),header), classTag[LoginResponse].runtimeClass).getBody.asInstanceOf[LoginResponse]

  }
}
