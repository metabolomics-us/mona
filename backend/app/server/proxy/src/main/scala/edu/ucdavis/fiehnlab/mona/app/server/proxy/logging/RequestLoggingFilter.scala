package edu.ucdavis.fiehnlab.mona.app.server.proxy.logging

import javax.servlet.FilterChain
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.{ContentCachingRequestWrapper, WebUtils}

/**
  * Created by sajjan on 12/15/16.
  * http://stackoverflow.com/a/39207422/406772
  */
@Component
class RequestLoggingFilter extends OncePerRequestFilter with Ordered {

  private val requestBeginTime = new ThreadLocal[Long]

  private val order = Ordered.LOWEST_PRECEDENCE - 10

  @Autowired
  val loggingService: LoggingService = null

  /**
    *
    * @param request
    * @param response
    */
  override def doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain): Unit = {

    logger.info("Starting filter")

    val isFirstRequest: Boolean = !isAsyncDispatch(request)

    // Add caching wrapper to request and response if necessary
    val cachedRequest: HttpServletRequest =
      if (isFirstRequest && !request.isInstanceOf[ContentCachingRequestWrapper])
        new ContentCachingRequestWrapper(request)
      else
        request

    // Time request
    requestBeginTime.set(System.currentTimeMillis())

    try {
      logger.debug("Running logging servlet for "+ cachedRequest.getSession.getId)
      filterChain.doFilter(cachedRequest, response)
    } finally {
      if (!isAsyncStarted(cachedRequest)) {
        logRequest(cachedRequest, response, System.currentTimeMillis() - requestBeginTime.get())
      }
    }
  }

  /**
    *
    * @param request
    * @param response
    * @param duration
    */
  private def logRequest(request: HttpServletRequest, response: HttpServletResponse, duration: Long): Unit = {

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

  def getOrder: Int = this.order
}
