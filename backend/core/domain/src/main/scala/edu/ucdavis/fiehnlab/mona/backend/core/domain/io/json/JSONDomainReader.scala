package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json

import java.io.Reader
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.reflect._

import com.fasterxml.jackson.databind.{DeserializationFeature, DeserializationConfig, ObjectMapper}
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.{DomainReadEventHandler, DomainReader}

import scala.reflect.ClassTag

/**
  * Created by wohlgemuth on 2/25/16.
  */
class JSONDomainReader[T:ClassTag](mapper: ObjectMapper) extends DomainReader[T]{

  mapper.registerModule(DefaultScalaModule)

  //required, in case we are provided with a list of value
  mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  /**
    * memory efficient reader
    *
    * @param input
    * @param handler
    */
  override def read(input: Reader, handler: DomainReadEventHandler[T]): Unit = {
    val value:T =  mapper.readValue(input,classTag[T].runtimeClass).asInstanceOf[T]

    handler.readEvent(value)
  }
}

/**
  * simple way to create an DomainReader
  */
object JSONDomainReader {

  def create[T:ClassTag] = {

    val mapper = new ObjectMapper() with ScalaObjectMapper

    new JSONDomainReader[T](mapper)

  }
}