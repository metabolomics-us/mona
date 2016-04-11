package edu.ucdavis.fiehnlab.mona.backend.core.domain.event

import java.util.Date

import scala.beans.BeanProperty
import scala.reflect.ClassTag

/**
  * a simple event, to be utilized for processing by other applications
  *
  * @param content
  * @tparam T
  */
case class Event[T : ClassTag](@BeanProperty content:T, @BeanProperty dateFired:Date = new Date(), @BeanProperty eventType:String = "None") extends Serializable


object Event{
  val ADD:String = "add"
  val DELETE:String = "delete"
  val UPDATE:String = "update"
}