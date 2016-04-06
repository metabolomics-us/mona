package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.events

import java.util.Date

/**
  * this object has been updated in the database
  *
  * @param content
  * @tparam T
  */
case class UpdateEvent[T](override val content:T) extends Event[T](content, new Date)
