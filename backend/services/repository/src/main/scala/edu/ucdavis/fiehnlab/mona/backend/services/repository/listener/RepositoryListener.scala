package edu.ucdavis.fiehnlab.mona.backend.services.repository.listener

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBusListener, EventBus}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.services.repository.layout.FileLayout
import org.springframework.beans.factory.annotation.Autowired

/**
  * Created by wohlg_000 on 5/18/2016.
  */
class RepositoryListener  @Autowired()(val bus:EventBus[Spectrum]) extends EventBusListener[Spectrum](bus) {

  /**
    * defines the layout of the repository
    */
  @Autowired
  val layout:FileLayout = null

  /**
    * an element has been received from the bus and should be now processed
    *
    * @param event
    */
  override def received(event: Event[Spectrum]): Unit = {
    event.eventType match {
      //we only care about ADDs at this point in time
      case (Event.ADD | Event.UPDATE )=>
        //writes the spectra to the while
      case  Event.DELETE =>
        //deletes the spectrum
      case _ => //ignore not of interest
    }
  }

}