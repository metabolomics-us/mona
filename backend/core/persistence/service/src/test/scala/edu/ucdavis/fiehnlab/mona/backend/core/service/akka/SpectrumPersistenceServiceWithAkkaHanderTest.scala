package edu.ucdavis.fiehnlab.mona.backend.core.service.akka

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.service.{AbstractSpectrumPersistenceServiceTest, EmbeddedServiceConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.service.listener.AkkaEventScheduler
import org.junit.runner.RunWith
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlgemuth on 4/1/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[EmbeddedServiceConfig],classOf[AkkaTestConfiguration]))
class SpectrumPersistenceServiceWithAkkaHanderTest extends AbstractSpectrumPersistenceServiceTest with LazyLogging {
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "we must ensure" when {
    "that our event scheduler " should {
      "be of type AkkaEventScheduler" in {
        assert(spectrumPersistenceService.eventScheduler.isInstanceOf[AkkaEventScheduler[Spectrum]])
      }
    }
  }

}

@Configuration
class AkkaTestConfiguration {

  @Bean
  def eventScheduler:AkkaEventScheduler[Spectrum] = {
    new AkkaEventScheduler[Spectrum]
  }
}