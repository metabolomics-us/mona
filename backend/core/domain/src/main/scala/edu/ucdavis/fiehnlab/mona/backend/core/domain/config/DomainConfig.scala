package edu.ucdavis.fiehnlab.mona.backend.core.domain.config

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.springframework.context.annotation.{ComponentScan, Configuration, Primary, Bean}

/**
  * Created by wohlgemuth on 3/10/16.
  */

@Configuration
class DomainConfig {

  @Bean
  @Primary
  def objectMapper : ObjectMapper = MonaMapper.create

}
