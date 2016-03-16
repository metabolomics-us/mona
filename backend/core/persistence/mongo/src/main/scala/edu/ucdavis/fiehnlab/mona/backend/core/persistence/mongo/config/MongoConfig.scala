package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config

import com.mongodb.{Mongo}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.{ISubmitterMongoRepository, ISpectrumMongoRepositoryCustom}
import org.springframework.beans.factory.annotation.{Value}
import org.springframework.context.annotation.{Bean, ComponentScan, Import, Configuration}
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
class MongoConfig {

  @Bean
  def mongoDatabase() = "mona"

  @Bean
  def mongoTemplate(mongo:Mongo,mongoDatabase:String) : MongoTemplate = new MongoTemplate(mongo,mongoDatabase)
}
