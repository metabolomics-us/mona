package edu.ucdavis.fiehnlab.mona.app.server.proxy.logging

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.netflix.zuul.exception.ZuulException
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.util.{ContentCachingRequestWrapper, ContentCachingResponseWrapper, WebUtils}

/**
  * Created by sajjan on 12/15/16.
  * http://stackoverflow.com/a/39207422/406772
  */
class LoggableDispatcherServlet(val loggingService: LoggingService) extends DispatcherServlet {

  /**
    *
    * @param request
    * @param response
    */
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
    } catch {
      // TODO Doesn't work?
      case e: ZuulException =>
        logger.info("Zuul Forwarding Error - wait for Eureka to identify service")
    } finally {
      log(cachingRequest, cachingResponse, System.currentTimeMillis() - startTime)
      updateResponse(cachingResponse)
    }
  }

  /**
    *
    * @param request
    * @param response
    * @param duration
    */
  private def log(request: HttpServletRequest, response: HttpServletResponse, duration: Long): Unit = {

    // Get http properties
    val httpStatus: Int = response.getStatus
    val httpMethod: String = request.getMethod
    val requestURI: String = request.getRequestURI
    val requestQueryString: String = request.getQueryString

    // Get IP Address
    val ipAddress: String =
      if (request.getHeader("X-FORWARDED-FOR") != null)
        request.getHeader("X-FORWARDED-FOR")
      else
        request.getRemoteAddr

    // Extract cached POST data
    val requestWrapper: ContentCachingRequestWrapper = WebUtils.getNativeRequest(request, classOf[ContentCachingRequestWrapper])
    val postDataBuffer: Array[Byte] = requestWrapper.getContentAsByteArray

    val postData: String =
      if (postDataBuffer.isEmpty)
        null
      else
        new String(postDataBuffer, 0, postDataBuffer.length, requestWrapper.getCharacterEncoding)

    // Log request
    loggingService.logRequest(httpStatus, httpMethod, requestURI, requestQueryString, postData, ipAddress, duration)
  }

  /**
    *
    * @param response
    */
  private def updateResponse(response: HttpServletResponse): Unit = {
    WebUtils.getNativeResponse(response, classOf[ContentCachingResponseWrapper]).copyBodyToResponse()
  }
}
