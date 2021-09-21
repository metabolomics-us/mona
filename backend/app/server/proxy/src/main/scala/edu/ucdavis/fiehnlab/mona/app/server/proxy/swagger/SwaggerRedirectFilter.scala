package edu.ucdavis.fiehnlab.mona.app.server.proxy.swagger

import javax.servlet.http.HttpServletRequest

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.typesafe.scalalogging.LazyLogging
import springfox.documentation.swagger2.web.Swagger2Controller

class SwaggerRedirectFilter extends ZuulFilter with LazyLogging {

  override def filterOrder(): Int = 0

  override def filterType(): String = "route"

  override def shouldFilter(): Boolean =
    RequestContext.getCurrentContext.getRequest.getRequestURI.endsWith(Swagger2Controller.DEFAULT_URL)


  override def run(): Object = {
    val ctx: RequestContext = RequestContext.getCurrentContext
    val request: HttpServletRequest = ctx.getRequest

    val newURI: String = request.getRequestURI.split(Swagger2Controller.DEFAULT_URL).head + Swagger2Controller.DEFAULT_URL

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
