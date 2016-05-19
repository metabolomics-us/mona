package edu.ucdavis.fiehnlab.mona.backend.services.repository.listener

import java.io.File

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBus, EventBusListener}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.services.repository.layout.FileLayout
import org.springframework.beans.factory.annotation.Autowired

/**
  * Created by wohlg_000 on 5/18/2016.
  */
class RepositoryListener  @Autowired()(val bus:EventBus[Spectrum], val layout:FileLayout) extends EventBusListener[Spectrum](bus) {

  /**
    * an element has been received from the bus and should be now processed
    *
    * @param event
    */
  override def received(event: Event[Spectrum]): Unit = {
    val file = new File(layout.layout(event.content),s"${event.content.splash.splash}")

    event.eventType match {
      //we only care about ADDs at this point in time
      case (Event.ADD | Event.UPDATE )=>
        //writes the spectra to the while
        logger.debug(s"writing spectrum with id ${event.content.id}")
        file.mkdirs()
        objectMapper.writeValue(file,event.content)
      case  Event.DELETE =>
        if(file.exists()){
          logger.debug(s"deleted spectrum with id ${event.content.id}")
          file.delete()
        }
      case _ => //ignore not of interest
    }
  }

}