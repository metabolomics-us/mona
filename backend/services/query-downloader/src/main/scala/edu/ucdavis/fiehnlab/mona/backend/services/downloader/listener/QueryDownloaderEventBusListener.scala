package edu.ucdavis.fiehnlab.mona.backend.services.downloader.listener

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBusListener, EventBus}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


/**
  * Created by sajjan on 5/16/16.
  */
/*
@Component
class QueryDownloaderEventBusListener @Autowired()(val bus: EventBus[Spectrum]) extends EventBusListener[Spectrum](bus) {

  @Autowired
  val queryDownloadSchedulerService: QueryDownloadSchedulerService = null

  /**
    * an element has been received from the bus and should be now processed
    *
    * @param event
    */
  override def received(event: Event[Spectrum]): Unit = {
    event.eventType match {
      case Event.ADD =>
        println("TEST")

      case _ =>
    }
  }
}
*/