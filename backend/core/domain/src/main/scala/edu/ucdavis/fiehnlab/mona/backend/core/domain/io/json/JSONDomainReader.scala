package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json

import java.io.Reader
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.MetaData

import scala.reflect._

import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.{DomainReadEventHandler, DomainReader}

import scala.reflect.ClassTag

/**
  * Created by wohlgemuth on 2/25/16.
  */
class JSONDomainReader[T: ClassTag](mapper: ObjectMapper) extends DomainReader[T] {

  /**
    * memory efficient reader
    *
    * @param input
    * @param handler
    */
  override def read(input: Reader, handler: DomainReadEventHandler[T]): Unit = {
    val value: T = mapper.readValue(input, classTag[T].runtimeClass).asInstanceOf[T]

    handler.readEvent(value)
  }
}

/**
  * default mapper for everything mona used
  */
object MonaMapper {
  def create: ObjectMapper = {

    println("creating new mapper")
    val mapper = new ObjectMapper() with ScalaObjectMapper

    mapper.registerModule(DefaultScalaModule)

    //required, in case we are provided with a list of value
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    mapper
  }
}

/**
  * simple way to create an DomainReader
  */
object JSONDomainReader {

  def create[T: ClassTag] = {

    new JSONDomainReader[T](MonaMapper.create)

  }
}

/**
  * tries to convert a number or boolean object from a string
  */
class NumberDeserializer extends JsonDeserializer[Any] {
  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Any = {
    val jsonNode: JsonNode = jsonParser.getCodec.readTree(jsonParser)

    val content = jsonNode.textValue

    if (content.toLowerCase.equals("true")) {
      true
    }
    else if (content.toLowerCase().equals("false")) {
      false
    }
    else {
      try {
        content.toInt
      } catch {
        case e: NumberFormatException => try {
          content.toDouble
        }
        catch {
          case e2: NumberFormatException => content
        }
      }
    }
  }
}