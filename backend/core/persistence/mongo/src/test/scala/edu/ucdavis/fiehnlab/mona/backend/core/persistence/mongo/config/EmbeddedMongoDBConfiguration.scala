package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config

import com.typesafe.scalalogging.LazyLogging
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Configuration, Import}

/**
  * Created by wohlg on 3/11/2016.
  */
@EnableAutoConfiguration
@Import(Array(classOf[MongoConfig]))
@Configuration
class EmbeddedMongoDBConfiguration extends LazyLogging{

}
