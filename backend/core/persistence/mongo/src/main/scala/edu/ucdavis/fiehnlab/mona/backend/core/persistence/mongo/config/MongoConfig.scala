package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config

import com.mongodb.{Mongo, MongoClient}
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.ISpectrumMongoRepositoryCustom
import org.springframework.beans.factory.annotation.{Value}
import org.springframework.context.annotation.{Import, Configuration}
import org.springframework.data.mongodb.config.AbstractMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
  * mongo specific database configuratoin
  */
@Configuration
@Import(Array(classOf[CascadeConfig]))
@EnableMongoRepositories(basePackageClasses = Array(
  classOf[ISpectrumMongoRepositoryCustom]
), excludeFilters = Array())
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
