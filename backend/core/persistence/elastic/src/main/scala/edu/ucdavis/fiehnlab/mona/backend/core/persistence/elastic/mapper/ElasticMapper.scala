package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.module.SimpleModule
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.{Value, MetaData}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{NumberDeserializer, MonaMapper}
import org.springframework.data.elasticsearch.core.EntityMapper

/**
  * this class is needed to tell elastic search what kind of mapper we want to use
  */
class EntityMapperImpl() extends EntityMapper {

  val mapper = MonaMapper.create
  val module = new SimpleModule()

  module.addSerializer(classOf[Value], new ElasticMetaDataSerializer)
  module.addDeserializer(classOf[Value], new ElasticMedaDataDeserializer)

  mapper.registerModule(module)

  override def mapToString(`object`: scala.Any): String = mapper.writeValueAsString(`object`)

  override def mapToObject[T](source: String, clazz: Class[T]): T = mapper.readValue(source, clazz)
}

/**
  * generates JSON sub entries for us, under the value element
  */
class ElasticMetaDataSerializer extends JsonSerializer[Value] with LazyLogging {
  override def serialize(t: Value, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider): Unit = {

    jsonGenerator.writeStartObject()
    t.value match {

      case x: Boolean => jsonGenerator.writeBooleanField("value_boolean", x)
      case x: Double => jsonGenerator.writeNumberField("value_number", x)
      case x: Long => jsonGenerator.writeNumberField("value_number", x)
      case x: Int => jsonGenerator.writeNumberField("value_number", x)
      case x: Float => jsonGenerator.writeNumberField("value_number", x)
      case _ =>
        jsonGenerator.writeStringField("value_text", t.value.toString)
    }
    jsonGenerator.writeEndObject()

  }
}

/**
  * this is reading the value element back again
  */
class ElasticMedaDataDeserializer extends JsonDeserializer[Value] with LazyLogging {
  val numberDeserializer: NumberDeserializer = new NumberDeserializer

  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Value = {

    logger.info("desializing....")
    new Value(0)
  }
}
