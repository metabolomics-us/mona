package edu.ucdavis.fiehnlab.mona.backend.core.statistics.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration
import org.springframework.context.annotation.Import
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{ComponentScan, Configuration}
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
  * Created by wohlgemuth on 3/21/16.
  */
@ComponentScan(basePackages = Array("edu.ucdavis.fiehnlab.mona.backend.core.statistics"))
@Configuration
@EnableAutoConfiguration
@Import(Array(classOf[PostgresqlConfiguration]))
class StatisticsRepositoryConfig
