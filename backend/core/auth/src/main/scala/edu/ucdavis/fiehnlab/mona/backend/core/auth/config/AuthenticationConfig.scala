package edu.ucdavis.fiehnlab.mona.backend.core.auth.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import org.springframework.context.annotation._
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories


/**
  * Created by wohlgemuth on 3/24/16.
  */
@Configuration
@Import(Array(classOf[MongoConfig]))
@EnableMongoRepositories(basePackageClasses = Array(classOf[UserRepository]))
@ComponentScan(basePackageClasses = Array(classOf[UserRepository]))
class AuthenticationConfig {

}
