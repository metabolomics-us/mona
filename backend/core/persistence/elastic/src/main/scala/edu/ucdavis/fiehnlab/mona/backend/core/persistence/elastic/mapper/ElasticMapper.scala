package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper

import java.lang.reflect.Field
import java.util

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.ser.{BeanSerializerModifier, BeanSerializer}
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.{MetaData}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.annotation.TupleSerialize
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{NumberDeserializer, MonaMapper}
import org.springframework.data.elasticsearch.core.EntityMapper

/**
  * custom serializer, which transform tuples, so that elastic search has only one value for mapping
  */
class ElasticMetaDataSerializer extends JsonSerializer[MetaData] with LazyLogging {
  override def serialize(value: MetaData, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider): Unit = {

    jsonGenerator.writeStartObject()
    val fields: Array[Field] = value.getClass.getDeclaredFields

    for (field <- fields) {
      try {
        field.setAccessible(true)
        //check for annotation
        if (field.getAnnotation(classOf[TupleSerialize]) != null) {
          val t = field.get(value)

          t match {
            case x: java.lang.Boolean => jsonGenerator.writeBooleanField("value_boolean", x)
            case x: java.lang.Double => jsonGenerator.writeNumberField("value_number", x)
            case x: java.lang.Long => jsonGenerator.writeNumberField("value_number", x.toString.toDouble)
            case x: java.lang.Integer => jsonGenerator.writeNumberField("value_number", x.toString.toDouble)
            case x: java.lang.Float => jsonGenerator.writeNumberField("value_number", x.toString.toDouble)
            case _ =>
              if(t != null) {
                jsonGenerator.writeStringField("value_text", t.toString)
              }
              else{
                throw new RuntimeException(s"object was null for ${value}, which is not permitted!")
              }
          }
        }
        //write normal fields
        else {
          jsonGenerator.writeObjectField(field.getName, field.get(value))
        }
      }
      catch {
        case e: IllegalArgumentException =>
          logger.warn(e.getMessage, e)
        case e: IllegalAccessException =>
          logger.warn(e.getMessage, e)
      }
    }
    jsonGenerator.writeEndObject()
  }
}

/**
  * builds a meta data object based on it's given properties and tries to evalualte the value correctly for the
  * different representations
  */
class ElasticMedaDataDeserializer extends JsonDeserializer[MetaData] with LazyLogging {
  val numberDeserializer: NumberDeserializer = new NumberDeserializer
  val monaMapper = MonaMapper.create

  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): MetaData = {
    val jsonNode: JsonNode = monaMapper.readTree(jsonParser)

    //our result object
    val metaData = monaMapper.treeToValue(jsonNode, classOf[MetaData])

    //lets find our custom fields and map them to the value
    val text = jsonNode.get("value_text")
    val number = jsonNode.get("value_number")
    val boolean = jsonNode.get("value_boolean")

    if (text != null) {
      metaData.copy(value = text.asText())
    }
    else if (number != null) {
      try {
        metaData.copy(value = number.toString.toInt)
      }
      catch {
        case x: NumberFormatException => {
          //in case it's stored as 123.0, which technically is an int
          if (number.toString.endsWith(".0")) {
            metaData.copy(value = number.toString.substring(0, number.toString.indexOf(".")).toInt)
          }
          else {
            metaData.copy(value = number.asDouble())
          }
        }
      }
    }
    else if (boolean != null) {
      metaData.copy(value = boolean.asBoolean())
    }
    else {
      logger.warn("we found no custom field, investigate! returning default object")
      metaData
    }
  }
}
