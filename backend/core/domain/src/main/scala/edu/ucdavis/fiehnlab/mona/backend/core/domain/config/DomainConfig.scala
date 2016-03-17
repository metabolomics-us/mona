package edu.ucdavis.fiehnlab.mona.backend.core.domain.config

import com.fasterxml.jackson.databind.{SerializationConfig, ObjectMapper}
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.springframework.context.annotation.{ComponentScan, Configuration, Primary, Bean}

/**
  * Created by wohlgemuth on 3/10/16.
  */

@Configuration
class DomainConfig extends LazyLogging{

  @Bean
  def objectMapper : ObjectMapper = {
    logger.debug("creating new custom object mapper")
    MonaMapper.create
  }

}
