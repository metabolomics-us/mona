package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config

import com.mongodb.{Mongo, MongoClient}
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.{ISpectrumMongoRepositoryCustom, ISubmitterMongoRepository}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
  * mongo specific database configuratoin
  */
@Configuration
@Import(Array(classOf[DomainConfig]))
@EnableMongoRepositories(basePackageClasses = Array(
  classOf[ISpectrumMongoRepositoryCustom]
), excludeFilters = Array())
@ComponentScan(basePackageClasses = Array(classOf[ISubmitterMongoRepository]))
class MongoConfig extends LazyLogging{

  val host:String = "192.168.99.100"

  @Bean
  def mongo:Mongo = {
    logger.warn(s"host: ${host}")
     new MongoClient(host, 27017)
  }
  @Bean
  def mongoDatabase() = "mona"

  @Bean
  def mongoTemplate(mongo:Mongo,mongoDatabase:String) : MongoTemplate = new MongoTemplate(mongo,mongoDatabase)
}
