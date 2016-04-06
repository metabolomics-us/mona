package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.events

import java.util.Date

/**
  * this object has been removed from the database
  *
  * @param content
  * @tparam T
  */
case class DeleteEvent[T](override val content:T) extends Event[T](content, new Date)
