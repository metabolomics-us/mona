package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest

import java.net.{NetworkInterface, UnknownHostException}

import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.commons.util.InetUtils
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean
import org.springframework.context.annotation.{Bean, Configuration, Primary, Profile}

import scala.collection.JavaConverters._

/**
  * Identifies the correct ip address associated with the docker container's hostname to
  * solve the issue of unreachable services when deploying with docker swarm
  * https://github.com/spring-cloud/spring-cloud-netflix/issues/1820
  * Created by sajjan on 12/11/17.
  */
@Configuration
@EnableDiscoveryClient
@Profile(Array("docker"))
class EurekaClientConfig extends LazyLogging {

  @Value("${server.port}")
  private val port: String = null

  @Bean
  @Primary
  def eurekaInstanceConfigBean(inetUtils: InetUtils): EurekaInstanceConfigBean = {
    val hostName: String = System.getenv("HOSTNAME")

    logger.info(s"Hostname: $hostName")

    // Find the host address that matches the hostname of the current container
    val hostAddress: Option[String] = NetworkInterface.getNetworkInterfaces.asScala.toSeq
      .flatMap(_.getInetAddresses.asScala)
      .filter(_.getHostName == hostName)
      .map(_.getHostAddress)
      .headOption

    if (hostAddress.isDefined) {
      logger.info(s"Registering with Eureka using: ${hostAddress.get} $hostName")

      val instance: EurekaInstanceConfigBean = new EurekaInstanceConfigBean(inetUtils)
      instance.setHostname(hostName)
      instance.setIpAddress(hostAddress.get)
      instance.setNonSecurePort(port.toInt)
      instance
    } else {
      throw new UnknownHostException(s"Cannot find IP address for hostname: $hostName")
    }
  }
}