package edu.ucdavis.fiehnlab.mona.app.server.proxy.logging

/**
  * Created by sajjan on 12/15/16.
  */
case class LogMessage(
                       httpStatus: Int,
                       httpMethod: String,
                       path: String,
                       queryString: String,
                       postData: String,
                       clientIp: String,
                       duration: Long
                     )