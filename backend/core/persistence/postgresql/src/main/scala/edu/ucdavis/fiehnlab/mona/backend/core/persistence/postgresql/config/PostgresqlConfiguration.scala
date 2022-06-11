package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config

import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.{News, Sequence, SpectrumFeedback, SpectrumResult, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{NewsRepository, SequenceRepository, SpectrumFeedbackRepository, SpectrumResultRepository, SubmitterRepository}
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.{ComponentScan, Configuration, Import}
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EntityScan(basePackages = Array("edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain"))
@Configuration
@EnableAutoConfiguration
@Import(Array(classOf[DomainConfig]))
@ComponentScan
@EnableJpaRepositories(basePackages = Array("edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository"))
class PostgresqlConfiguration {

}
