package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.events

import java.util.Date

/**
  * an addition was done
  *
  * @param content
  * @tparam T
  */
case class AddEvent[T](override val content:T) extends Event[T](content, new Date)
