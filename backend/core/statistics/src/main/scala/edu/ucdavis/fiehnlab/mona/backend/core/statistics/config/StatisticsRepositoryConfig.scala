package edu.ucdavis.fiehnlab.mona.backend.core.statistics.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration
import org.springframework.context.annotation.Import
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.{ComponentScan, Configuration}
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.context.annotation.Profile

/**
  * Created by wohlgemuth on 3/21/16.
  */
@EntityScan(basePackages = Array("edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain"))
@ComponentScan(basePackages = Array("edu.ucdavis.fiehnlab.mona.backend.core.statistics"))
@EnableAutoConfiguration
@Configuration
@Import(Array(classOf[PostgresqlConfiguration]))
@Profile(Array("mona.persistence"))
class StatisticsRepositoryConfig
