package edu.ucdavis.fiehnlab.mona.backend.core.statistics

import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation.Import
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration

/**
  * Created by wohlg on 3/20/2016.
  */
@SpringBootApplication(scanBasePackageClasses = Array())
@Import(Array(classOf[PostgresqlConfiguration]))
class TestConfig {

}
