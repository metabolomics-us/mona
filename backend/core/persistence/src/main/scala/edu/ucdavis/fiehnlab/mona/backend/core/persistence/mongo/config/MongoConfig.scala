package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config

import com.mongodb.{Mongo, MongoClient}
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoConfiguration

/**
  * Created by wohlgemuth on 2/29/16.
  */
@Configuration
@ConfigurationProperties
class MongoConfig extends AbstractMongoConfiguration {

  val server: String  = scala.util.Properties.envOrElse("MONGO_SERVER", "127.0.0.1" )

  val database: String  = scala.util.Properties.envOrElse("MONGO_DATABASE", "mona-test" )

  override def mongo(): Mongo = new MongoClient(server)

  override def getDatabaseName: String = database
}
