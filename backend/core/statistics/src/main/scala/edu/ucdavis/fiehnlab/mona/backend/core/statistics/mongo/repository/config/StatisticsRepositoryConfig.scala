package edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.repository.config

import edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.repository.aggregation.IStatisticsMongoRepository
import org.springframework.context.annotation.{ComponentScan, Configuration}
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
  * Created by wohlgemuth on 3/21/16.
  */
@ComponentScan
@Configuration
@EnableMongoRepositories(basePackageClasses = Array(
  classOf[IStatisticsMongoRepository]
), excludeFilters = Array())
class StatisticsRepositoryConfig {

}
