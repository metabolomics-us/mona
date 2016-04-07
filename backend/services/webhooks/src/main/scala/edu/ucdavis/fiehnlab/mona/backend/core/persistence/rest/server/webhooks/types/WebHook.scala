package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import scala.annotation.meta.field

/**
  * defines an internal or external webhook to be notified in case of observed events
  *
  * @param name
  */
@Document(collection = "WEBHOOK")
case class WebHook(
                    @(Id@field)
                    name:String,
                    url:String,
                    description:String
                  )
