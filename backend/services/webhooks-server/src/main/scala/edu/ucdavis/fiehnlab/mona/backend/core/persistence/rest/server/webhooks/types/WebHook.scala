package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types

import java.util.Date

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
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
                    name: String,
                    url: String,
                    description: String = "None provided"
                  )

/**
  * the result of a triggered webhook
  *
  * @param url
  * @param success
  * @param error
  * @param invoked when was this hook executed last
  */
@Document(collection = "WEBHOOKS_TRIGGERED")
case class WebHookResult(
                          @(Id@field)
                          id: String,
                          @(Indexed@field)
                          name: String,
                          url: String,
                          success: Boolean = true,
                          error: String = "",
                          @(Indexed@field)
                          invoked: Date = new Date()
                        )