package edu.ucdavis.fiehnlab.mona.backend.core.statistics.config

import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.aggregation.IStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.{MetaDataStatisticsMongoRepository, TagStatisticsMongoRepository}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{ComponentScan, Configuration}
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
  * Created by wohlgemuth on 3/21/16.
  */
@ComponentScan(Array("edu.ucdavis.fiehnlab.mona.backend.core.statistics"))
@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackageClasses = Array(classOf[IStatisticsMongoRepository], classOf[MetaDataStatisticsMongoRepository], classOf[TagStatisticsMongoRepository]), excludeFilters = Array())
class StatisticsRepositoryConfig
