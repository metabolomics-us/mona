package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.reflection.CascadeSaveMongoEventListener
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * Created by wohlgemuth on 3/7/16.
  */
@Configuration
class CascadeConfig {

  @Bean
  def cascadingMongoEventListener: CascadeSaveMongoEventListener = {
    new CascadeSaveMongoEventListener()
  }
}
