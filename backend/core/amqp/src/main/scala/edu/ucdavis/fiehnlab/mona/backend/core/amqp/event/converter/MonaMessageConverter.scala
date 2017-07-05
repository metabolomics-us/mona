package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.converter

import java.io.ByteArrayOutputStream

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.springframework.amqp.core.{Message, MessageProperties}
import org.springframework.amqp.support.converter.AbstractMessageConverter

/**
  * a simple generic message converter
  */
class MonaMessageConverter extends AbstractMessageConverter {

  val objectMapper: ObjectMapper = MonaMapper.create

  override def fromMessage(message: Message): AnyRef = {
    objectMapper.readValue(message.getBody, classOf[Any]).asInstanceOf[AnyRef]
  }

  override def createMessage(content: scala.Any, messageProperties: MessageProperties): Message = {

    val stream = new ByteArrayOutputStream()
    objectMapper.writeValue(stream, content)

    new Message(stream.toByteArray, new MessageProperties)
  }
}
