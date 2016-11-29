package edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.config

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBus, ReceivedEventCounter}
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{BusConfig, MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.EventScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config.ElasticsearchConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.counter.SequenceService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.listener.AkkaEventScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.persistence.SpectrumPersistenceService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.synchronization.SpectrumElasticEventListener
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}

/**
  * Created by wohlg on 3/16/2016.
  */
@ComponentScan(basePackageClasses = Array(classOf[SpectrumPersistenceService], classOf[SpectrumElasticEventListener], classOf[EventScheduler[Spectrum]], classOf[SequenceService]))
@Configuration
@Import(Array(classOf[ElasticsearchConfig], classOf[MongoConfig], classOf[MonaEventBusConfiguration],classOf[MonaNotificationBusConfiguration]))
class PersistenceServiceConfig {

  @Bean
  def eventScheduler:AkkaEventScheduler[Spectrum] = {
    new AkkaEventScheduler[Spectrum]
  }
}
