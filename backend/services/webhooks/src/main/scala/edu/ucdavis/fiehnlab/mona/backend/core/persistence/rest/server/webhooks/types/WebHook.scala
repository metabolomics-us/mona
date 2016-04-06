package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types

import org.springframework.data.mongodb.core.mapping.Document

/**
  * defines an internal or external webhook to be notified in case of observed events
  *
  * @param name
  */
@Document(collection = "WEBHOOK")
case class WebHook(
                    id:String,
                    name:String,
                    description:String,
                    url:String
                  )
