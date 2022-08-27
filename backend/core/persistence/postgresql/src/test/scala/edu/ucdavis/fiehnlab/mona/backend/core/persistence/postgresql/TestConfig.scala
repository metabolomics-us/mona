package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.MonaEventBusCounterConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

@SpringBootApplication(scanBasePackageClasses = Array())
@Import(Array(classOf[PostgresqlConfiguration], classOf[MonaEventBusCounterConfiguration], classOf[DomainConfig]))
class TestConfig {
}
