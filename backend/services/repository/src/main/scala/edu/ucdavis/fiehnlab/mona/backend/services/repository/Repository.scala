package edu.ucdavis.fiehnlab.mona.backend.services.repository

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.MonaEventBusConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.config.CurationConfig
import edu.ucdavis.fiehnlab.mona.backend.services.repository.layout.{InchiKeyLayout, FileLayout}
import edu.ucdavis.fiehnlab.mona.backend.services.repository.listener.RepositoryListener
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Import, Bean}
import java.io.File

/**
  * Created by wohlg_000 on 5/18/2016.
  */
@SpringBootApplication
@Import(Array(classOf[RestClientConfig],classOf[CurationConfig],classOf[MonaEventBusConfiguration]))
class Repository {

  @Bean
  def fileLayout:FileLayout = {
    val temp =File.createTempFile("random","none")
    val dir =new File(temp.getParentFile,"mona")
    dir.mkdirs()
    new InchiKeyLayout(dir)
  }

  @Bean
  def repositoryListener(eventBus:EventBus[Spectrum], layout:FileLayout) : RepositoryListener = new RepositoryListener(eventBus,layout)
}

object Repository{

}
