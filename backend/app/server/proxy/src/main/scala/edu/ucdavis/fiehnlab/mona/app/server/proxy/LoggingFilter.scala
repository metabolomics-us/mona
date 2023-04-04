package edu.ucdavis.fiehnlab.mona.app.server.proxy

import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.stereotype.Component
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.route.Route
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.URI
import java.util.Collections
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR
import scala.jdk.CollectionConverters._
import scala.collection.mutable.Set


@Component
class LoggingFilter extends GlobalFilter {
  val log: Log = LogFactory.getLog(getClass)

  @Override
  def filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono[Void] = {
    val uris: Set[_] = exchange.getAttributeOrDefault(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, Collections.EMPTY_SET).asScala
    val originalUri: String = if (uris.isEmpty) exchange.getRequest.getURI.toString else uris.iterator.next().toString
    val route: Route = exchange.getAttribute(GATEWAY_ROUTE_ATTR)
    val routeUri: URI = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR)
    log.info("Incoming request " + originalUri + " is routed to id: " + route.getId + ", uri:" + routeUri)
    chain.filter(exchange)
  }
}
