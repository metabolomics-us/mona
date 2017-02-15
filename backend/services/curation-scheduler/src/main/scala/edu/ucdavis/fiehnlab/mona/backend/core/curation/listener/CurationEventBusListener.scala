package edu.ucdavis.fiehnlab.mona.backend.core.curation.listener

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBus, EventBusListener}
import edu.ucdavis.fiehnlab.mona.backend.core.curation.service.CurationService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * listens to the event bus and everytime it finds a matching message it will forward it to the
  * dedicated queue for curation tasks
  */
@Component
class CurationEventBusListener @Autowired()(val bus:EventBus[Spectrum]) extends EventBusListener[Spectrum](bus) {

  @Autowired
  val curationService: CurationService = null

  /**
    * an element has been received from the bus and should be now processed
    *
    * @param event
    */
  override def received(event: Event[Spectrum]): Unit = {
    event.eventType match {
        //we only care about ADDs at this point in time
      case Event.ADD =>
        curationService.scheduleSpectrum(event.content)

      case _ => //ignore not of interest
    }
  }
}
