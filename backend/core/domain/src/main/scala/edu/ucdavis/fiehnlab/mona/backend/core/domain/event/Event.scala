package edu.ucdavis.fiehnlab.mona.backend.core.domain.event

import java.util.Date

import scala.beans.BeanProperty

/**
  * a simple event, to be utilized for processing by other applications
  *
  * @param content
  * @tparam T
  */
case class Event[T](@BeanProperty content:T,@BeanProperty dateFired:Date,@BeanProperty eventType:String)


object Event{
  val ADD:String = "add"
  val DELETE:String = "delete"
  val UPDATE:String = "update"

}