package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service

import com.typesafe.scalalogging.LazyLogging

import java.util.Date
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.TokenService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Roles, Users}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginInfo
import io.jsonwebtoken.{Claims, Jwts, SignatureAlgorithm}
import org.apache.commons.lang3.time.DateUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

import scala.jdk.CollectionConverters._

/**
  * Created by wohlgemuth on 3/26/16.
  */
class JWTTokenService extends TokenService with LazyLogging{

  @Autowired
  val tokenSecret: TokenSecret = null

  /**
    * generates a token for us
    * based on the given user
    *
    * @param user
    * @return
    */
  override def generateToken(user: Users, timeOfLife: Int = 24 * 7): String = {

    val issueDate = new Date()
    val experiationDate = DateUtils.addHours(issueDate, timeOfLife)

    // associated roles
    val roles = user.getRoles.asScala.collect {
      case x: Roles => x.getName
    }.asJava

    Jwts.builder().setSubject(user.getEmailAddress).claim("roles", roles).setIssuedAt(issueDate).setExpiration(experiationDate).signWith(SignatureAlgorithm.HS256, tokenSecret.value).compact()
  }

  /**
    * provide us with some information about this token
    *
    * @param token
    * @return
    */
  override def info(token: String): LoginInfo = {
    val claims: Claims = Jwts.parser().setSigningKey(tokenSecret.value).parseClaimsJws(token).getBody
    LoginInfo(claims.getSubject, claims.getIssuedAt, claims.getExpiration, claims.get("roles").asInstanceOf[java.util.List[String]])
  }
}
