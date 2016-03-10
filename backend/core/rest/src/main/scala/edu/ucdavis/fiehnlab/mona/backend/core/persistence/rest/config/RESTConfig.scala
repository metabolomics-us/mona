package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.config

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.CascadeConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.{Import, Primary, Bean, Configuration}
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
  * Created by wohlgemuth on 2/29/16.
  */
@ConfigurationProperties
@Import(Array(classOf[DomainConfig]))
class RESTConfig {

  val port:Int = 8080

}
