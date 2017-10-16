package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper

import java.lang.reflect.Field

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind._
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.annotation.AnalyzedStringSerialize

/**
  * Created by sajjan on 12/20/16.
  */
class AnalyzedStringSerializer[T] extends JsonSerializer[T] with LazyLogging {
  override def serialize(value: T, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider): Unit = {

    jsonGenerator.writeStartObject()
    val fields: Array[Field] = value.getClass.getDeclaredFields

    for (field <- fields) {
      try {
        field.setAccessible(true)

        if (field.getAnnotation(classOf[AnalyzedStringSerialize]) != null) {
          jsonGenerator.writeObjectField(s"${field.getName}_analyzed", field.get(value))
        }

        jsonGenerator.writeObjectField(field.getName, field.get(value))
      } catch {
        case e: IllegalArgumentException =>
          logger.warn(e.getMessage, e)
        case e: IllegalAccessException =>
          logger.warn(e.getMessage, e)
      }
    }

    jsonGenerator.writeEndObject()
  }
}