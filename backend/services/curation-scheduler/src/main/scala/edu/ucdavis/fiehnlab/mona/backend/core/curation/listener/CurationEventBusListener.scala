package edu.ucdavis.fiehnlab.mona.backend.core.curation.listener

import java.util.Date

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBus, EventBusListener}
import edu.ucdavis.fiehnlab.mona.backend.core.curation.service.CurationService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * listens to the event bus and every time it finds a matching message it will forward it to the
  * dedicated queue for curation tasks
  */
@Component
class CurationEventBusListener @Autowired()(val bus: EventBus[Spectrum]) extends EventBusListener[Spectrum](bus) {

  @Autowired
  val curationService: CurationService = null

  /**
    * Allow scheduling of curation if curation has never been scheduled or if it hasn't
    * been curated within the past 15 minutes
    * @param spectrum
    * @return
    */
  private def shouldScheduleCuration(spectrum: Spectrum): Boolean = {
    spectrum.lastCurated == null || (new Date().getTime - spectrum.lastCurated.getTime) / (60 * 1000) >= 15
  }

  /**
    * an element has been received from the bus and should be now processed
    *
    * @param event
    */
  override def received(event: Event[Spectrum]): Unit = {
    event.eventType match {
      case Event.ADD | Event.UPDATE if shouldScheduleCuration(event.content) =>
        curationService.scheduleSpectrum(event.content)

      case _ =>
    }
  }
}
