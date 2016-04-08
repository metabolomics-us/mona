package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.listener

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBus, EventBusListener}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.service.WebHookService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * listens to the internally defined event bus and triggers notifications of the webhooks everytime
  * it receives a message from the mona bus
  */
@Component
class WebHookEventBusListener @Autowired()(val bus: EventBus[Spectrum]) extends EventBusListener[Spectrum](bus) {

  @Autowired
  val webHookService: WebHookService = null

  def received(event: Event[Spectrum]): Unit = {
    logger.debug(s"received event with id: ${event.content.id}")
    webHookService.trigger(event.content.id)
  }

}
