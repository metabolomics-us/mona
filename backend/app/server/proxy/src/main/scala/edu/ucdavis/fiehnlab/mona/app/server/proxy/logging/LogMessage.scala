package edu.ucdavis.fiehnlab.mona.app.server.proxy.logging

import java.util.Date

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import scala.annotation.meta.field

/**
  * Created by sajjan on 12/15/16.
  */
@Document(collection = "LOGS")
case class LogMessage(
                       @(Id@field)
                       id: String,
                       httpStatus: Int,
                       httpMethod: String,
                       path: String,
                       queryString: String,
                       postData: String,

                       clientIp: String,
                       clientCountry: String,
                       clientRegion: String,
                       clientCity: String,

                       duration: Long,
                       date: Date
                     )