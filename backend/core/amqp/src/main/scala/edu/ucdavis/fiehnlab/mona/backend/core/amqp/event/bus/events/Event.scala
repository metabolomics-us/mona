package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.events

import java.util.Date

/**
  * a simple event, to be utilized for processing by other applications
  *
  * @param content
  * @tparam T
  */
abstract class Event[T](val content:T, dateFired:Date) extends Serializable
