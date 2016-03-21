package edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.repository.IStatisticsMongoRepository
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Configuration, Import}
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@EnableMongoRepositories(basePackageClasses = Array(
  classOf[ISpectrumMongoRepositoryCustom],
  classOf[IStatisticsMongoRepository]
))
@EnableAutoConfiguration
@Import(Array(classOf[MongoConfig]))
@Configuration
class EmbeddedMongoDBConfiguration extends LazyLogging{

}
