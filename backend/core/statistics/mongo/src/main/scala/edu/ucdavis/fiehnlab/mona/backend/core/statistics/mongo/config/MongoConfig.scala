package edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.config

import com.mongodb.Mongo
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.repository.IStatisticsMongoRepository
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@Import(Array(classOf[DomainConfig]))
@EnableMongoRepositories(basePackageClasses = Array(
  classOf[IStatisticsMongoRepository]
), excludeFilters = Array())
class MongoConfig {

  @Bean
  def mongoDatabase() = "mona"

  @Bean
  def mongoTemplate(mongo:Mongo,mongoDatabase:String) : MongoTemplate = new MongoTemplate(mongo,mongoDatabase)
}
