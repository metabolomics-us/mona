package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config

import com.mongodb.{Mongo, MongoClient}
import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.{Value, Autowired}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.{ComponentScan, Configuration}
import org.springframework.data.mongodb.config.AbstractMongoConfiguration

/**
  * mongo specific database configuratoin
  */
@Configuration
class MongoConfig extends AbstractMongoConfiguration with LazyLogging {

  @Value("${mona.persistence.host}")
  val server: String = null

  @Value("${mona.persistence.database}")
  val database: String = null

  override def mongo(): Mongo = {
    logger.info(s"creating new client to server: ${server}")
    new MongoClient(server)
  }

  override def getDatabaseName: String = {
    logger.info(s"utilizing database: ${database}")
    database
  }
}
