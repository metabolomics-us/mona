package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service

import java.util.Date

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.TokenService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import io.jsonwebtoken.{Jwts, SignatureAlgorithm}
import org.apache.commons.lang.time.DateUtils
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 3/26/16.
  */
class JWTTokenService extends TokenService {


  @Autowired
  val tokenSecret: TokenSecret = null

  /**
    * time how long a token is valid for in hours
    */
  val timeOfLife = 24*7

  /**
    * generates a token for us
    * based on the given user
    *
    * @param user
    * @return
    */
  override def generateToken(user: User): String = {

    val issueDate = new Date()
    val experiationDate = DateUtils.addHours(issueDate, timeOfLife)

    //associated roles
    val roles = user.roles.asScala.collect {
      case x: Role => x.name
    }.asJava

    Jwts.builder().setSubject(user.username).claim("roles", roles).setIssuedAt(issueDate).setExpiration(experiationDate).signWith(SignatureAlgorithm.HS256, tokenSecret.value).compact()
  }
}
