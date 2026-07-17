package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config

import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.listener.AkkaEventScheduler
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import, Profile}
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.springframework.stereotype.Component
import org.springframework.boot.context.properties.ConfigurationProperties
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.EventScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.synchronization.CountListener

// DeletionQueueConfig is intentionally NOT scanned here. It declares a durable deletion queue and a
// SpectrumDeletionListener that must run in exactly one place (the persistence-server, where the delete
// controller lives). Scanning it from this shared config would start a competing consumer in every
// persistence service and collide its Queue bean with curation's queue. RestServerConfig @Import-s it instead
@EntityScan(basePackages = Array("edu.ucdavis.fiehnlab.mona.backend.core.domain"))
@Configuration
@Import(Array(classOf[DomainConfig], classOf[MonaEventBusConfiguration], classOf[MonaNotificationBusConfiguration]))
@ComponentScan(basePackageClasses = Array(classOf[SpectrumPersistenceService],  classOf[CountListener],
  classOf[EventScheduler[Spectrum]]))
@EnableJpaRepositories(basePackages = Array("edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository"))
@Profile(Array("mona.persistence"))
class PostgresqlConfiguration {

  @Bean
  def eventScheduler: AkkaEventScheduler[Spectrum] = {
    new AkkaEventScheduler[Spectrum]
  }
}

@Component
@ConfigurationProperties(prefix = "mona.persistence")
@Profile(Array("mona.persistence"))
class PostgresqlConfigurationProperties{

}
