package edu.ucdavis.fiehnlab.mona.app.server.proxy.swagger

import java.util
import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import springfox.documentation.swagger.web.{SwaggerResource, SwaggerResourcesProvider}

import scala.collection.JavaConverters._
import scala.collection.mutable

@Component
@Primary
class GatewaySwaggerResourcesProvider extends SwaggerResourcesProvider with LazyLogging {

  @Autowired
  val routeLocator: DiscoveryClientRouteLocator = null

  @Autowired
  val discoveryClient: DiscoveryClient = null

  @PostConstruct
  private def initialize(): Unit = get()


  override def get(): util.List[SwaggerResource] = {
    val resources: mutable.Map[String, SwaggerResource] = mutable.Map()

    logger.info("Getting Swagger resources")

    routeLocator.getRoutes.asScala.foreach { route =>
      logger.info("path: " + route.getPath + " and service " + route.getLocation)

      val services: Seq[ServiceInstance] = discoveryClient.getInstances(route.getLocation).asScala

      if (services.nonEmpty) {
        val location: String = "http://" + services.head.getHost + ":" + services.head.getPort + "/v2/api-docs"
        val newRoute: String = "/" + route.getLocation + "/v2/api-docs"

        resources(location) = swaggerResource(route.getLocation, newRoute)

        if (routeLocator.getMatchingRoute(newRoute) == null) {
          logger.info("adding new route: " + newRoute + " to location " + location)
          routeLocator.addRoute(newRoute, location)
        }
      }
    }

    resources.values.toList.asJava
  }

  private def swaggerResource(name: String, location: String, version: String = "2.0") = {
    val swaggerResource: SwaggerResource = new SwaggerResource
    swaggerResource.setName(name)
    swaggerResource.setLocation(location)
    swaggerResource.setSwaggerVersion(version)
    swaggerResource
  }
}
