package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json

import java.io.Reader

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.{DomainReadEventHandler, DomainReader}

import scala.reflect.{ClassTag, _}

/**
  * Created by wohlgemuth on 2/25/16.
  */
class JSONDomainReader[T: ClassTag](mapper: ObjectMapper) extends DomainReader[T] with LazyLogging {

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

    val mapper = new ObjectMapper() with ScalaObjectMapper

    mapper.registerModule(DefaultScalaModule)

    //required, in case we are provided with a list of value
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.setSerializationInclusion(Include.NON_NULL);
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
class NumberDeserializer extends JsonDeserializer[Any] with LazyLogging{

  /**
    * tries to convert a value to a number/boolean/text on the fly
    *
    * @param jsonParser
    * @param deserializationContext
    * @return
    */
  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Any = {
    val jsonNode: JsonNode = jsonParser.getCodec.readTree(jsonParser)

    if (jsonNode.isNumber) {
      jsonNode.numberValue()
    }
    else if(jsonNode.isBoolean){
      jsonNode.booleanValue()
    }
    else {
      val content = jsonNode.textValue()

      if (content != null) {
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
              case e2: NumberFormatException =>  content
            }
          }
        }
      }
      else {
         content
      }
    }
  }
}