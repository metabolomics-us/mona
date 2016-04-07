package edu.ucdavis.fiehnlab.mona.backend.core.service.config

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBus, EventBusCounter}
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.BusConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.EventScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config.ElasticsearchConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.service.persistence.SpectrumPersistenceService
import edu.ucdavis.fiehnlab.mona.backend.core.service.synchronization.SpectrumElasticEventListener
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}

/**
  * Created by wohlg on 3/16/2016.
  */
@ComponentScan(basePackageClasses = Array(classOf[SpectrumPersistenceService], classOf[SpectrumElasticEventListener], classOf[EventScheduler[Spectrum]], classOf[BusConfig]))
@Configuration
@Import(Array(classOf[ElasticsearchConfig], classOf[MongoConfig], classOf[BusConfig]))
class PersistenceServiceConfig {

  /**
    * required to notify the main event bus about spectra being modified events
    * and should ensure that all parts of the cluster stay synchronized
    *
    * @return
    */
  @Bean
  def eventBus: EventBus[Spectrum] = new EventBus[Spectrum]

  /**
    * counts all the events send over the event bus
    * which could be used later for metrics
    *
    * @return
    */
  @Bean
  def eventCounter: EventBusCounter[Spectrum] = new EventBusCounter[Spectrum]
}
