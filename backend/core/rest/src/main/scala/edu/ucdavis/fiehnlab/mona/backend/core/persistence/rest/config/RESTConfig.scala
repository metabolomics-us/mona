package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.config

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.{Primary, Bean, Configuration}
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
  * Created by wohlgemuth on 2/29/16.
  */
@ConfigurationProperties
class RESTConfig {

  val port:Int = 8080

  @Bean
  @Primary
  def objectMapper : ObjectMapper = MonaMapper.create

}
