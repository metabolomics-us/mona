package edu.ucdavis.fiehnlab.mona.backend.core.domain.io

import java.io.Reader

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

/**
  * Created by wohlgemuth on 2/25/16.
  */
abstract class DomainReader[T: ClassTag] {

  /**
    * reads the given dataset from the provided
    * reader. This can contain 0 to N objects of the type T
    * not recommended for very large amount of data
    *
    * @param input
    * @return
    */
  def read(input: Reader): T = {

    val buffer = new ListBuffer[T]

    read(input, new DomainReadEventHandler[T] {
      /**
        * add element to the list
        *
        * @param event
        */
      override def readEvent(event: T): Unit = {
        buffer += event
      }
    })

    buffer.toList.head
  }

  /**
    * memory efficient reader
    *
    * @param input
    * @param handler
    */
  def read(input: Reader, handler: DomainReadEventHandler[T]): Unit
}

/**
  * simple event handler, don't see to many use for different implementations for it
  * @param ev$1
  * @tparam T
  */
abstract class DomainReadEventHandler[T: ClassTag] {
  def readEvent(event: T): Unit
}