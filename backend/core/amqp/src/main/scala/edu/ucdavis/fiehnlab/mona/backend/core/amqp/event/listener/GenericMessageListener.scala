package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.springframework.amqp.core.{Message, MessageListener}

import scala.reflect._

/**
  * Created by wohlg on 4/18/2016.
  */
abstract class GenericMessageListener[T:ClassTag] extends MessageListener{

  val objectMapper:ObjectMapper = MonaMapper.create

  /**
    * converts messages for us from json to our actual content
    * @param message
    */
  override final def onMessage(message: Message): Unit = {
    val content:T = objectMapper.readValue(message.getBody,classTag[T].runtimeClass).asInstanceOf[T]

    handleMessage(content)
  }

  /**
    * works on the actual message
    * @param content
    */
  def handleMessage(content:T): Unit
}
