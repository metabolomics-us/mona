package edu.ucdavis.fiehnlab.mona.app.server.proxy.logging

import java.io.{BufferedReader, InputStreamReader}
import java.util.stream.Collectors
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.IOUtils
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.util.{ContentCachingRequestWrapper, ContentCachingResponseWrapper, WebUtils}

/**
  * Created by sajjan on 12/15/16.
  * http://stackoverflow.com/a/39207422/406772
  */
/
class LoggableDispatcherServlet extends DispatcherServlet {

  override def doDispatch(request: HttpServletRequest, response: HttpServletResponse): Unit = {

    // Add caching wrapper to request and response if necessary
    val cachingRequest: HttpServletRequest =
      if (!request.isInstanceOf[ContentCachingRequestWrapper])
        new ContentCachingRequestWrapper(request)
      else
        request

    val cachingResponse: HttpServletResponse =
      if (!response.isInstanceOf[ContentCachingResponseWrapper])
        new ContentCachingResponseWrapper(response)
      else
        response

    // Time request
    val startTime: Long = System.currentTimeMillis()

    try {
      super.doDispatch(cachingRequest, cachingResponse)
    } finally {
      log(cachingRequest, cachingResponse, System.currentTimeMillis() - startTime)
      updateResponse(cachingResponse)
    }
  }

  private def log(request: HttpServletRequest, response: HttpServletResponse, duration: Long): Unit = {

    // Extract cached POST data
    val requestWrapper: ContentCachingRequestWrapper = WebUtils.getNativeRequest(request, classOf[ContentCachingRequestWrapper])
    val postDataBuffer: Array[Byte] = requestWrapper.getContentAsByteArray

    val postData: String =
      if (postDataBuffer.isEmpty)
        null
      else
        new String(postDataBuffer, 0, postDataBuffer.length, requestWrapper.getCharacterEncoding)

    // Create logging message
    val logMessage: LogMessage = LogMessage(response.getStatus, request.getMethod, request.getRequestURI, request.getQueryString, postData, request.getRemoteAddr, duration)

    logger.info(logMessage)
  }

  private def updateResponse(response: HttpServletResponse): Unit = {
    WebUtils.getNativeResponse(response, classOf[ContentCachingResponseWrapper]).copyBodyToResponse()
  }
}
