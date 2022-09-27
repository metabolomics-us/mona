/*
package edu.ucdavis.fiehnlab.mona.app.server.proxy.swagger

import javax.servlet.http.HttpServletRequest

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.typesafe.scalalogging.LazyLogging

class SwaggerRedirectFilter extends ZuulFilter with LazyLogging {

  final val default_val: String = "/v3/api-docs"

  override def filterOrder(): Int = 0

  override def filterType(): String = "route"

  override def shouldFilter(): Boolean =
    RequestContext.getCurrentContext.getRequest.getRequestURI.endsWith(default_val)


  override def run(): Object = {
    val ctx: RequestContext = RequestContext.getCurrentContext
    val request: HttpServletRequest = ctx.getRequest

    val newURI: String = request.getRequestURI.split(default_val).head + default_val

    logger.info(s"Remapping ${request.getRequestURI}")

    try {
      ctx.put("requestURI", "")
    } catch {
      case e: Exception =>
        logger.error(s"Unable to remap ${request.getRequestURI} -> $newURI")
        e.printStackTrace()
    }

    null
  }
}
*/
