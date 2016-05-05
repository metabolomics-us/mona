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
case class Event[T : ClassTag](content:T, dateFired:Date = new Date(), eventType:String = "None") extends Serializable


object Event{
  val ADD:String = "add"
  val DELETE:String = "delete"
  val UPDATE:String = "update"
  val SYNC:String = "synchronize"

}