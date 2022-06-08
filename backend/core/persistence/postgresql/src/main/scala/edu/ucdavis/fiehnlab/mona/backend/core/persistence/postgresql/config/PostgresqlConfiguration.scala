package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config

import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.{ComponentScan, Configuration, Import}
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableAutoConfiguration
@Import(Array(classOf[DomainConfig]))
@ComponentScan
@EnableJpaRepositories(basePackageClasses = Array(classOf[SpectrumResultRepository]))
class PostgresqlConfiguration {

}
