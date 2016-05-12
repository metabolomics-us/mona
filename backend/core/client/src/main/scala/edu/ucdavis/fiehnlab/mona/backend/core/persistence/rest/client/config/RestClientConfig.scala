package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.service.RestLoginService
import org.apache.http.conn.HttpClientConnectionManager
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation._
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.{RestOperations, RestTemplate}

/**
  * Created by wohlg_000 on 3/2/2016.
  */
@Configuration
@Import(Array(classOf[DomainConfig]))
class RestClientConfig extends LazyLogging {

  @Value("${mona.rest.client.connections.total:4}")
  val monaMaxConnections: Int = 0

  @Value("${mona.rest.client.connections.route:1}")
  val monaMaxRouteConnections: Int = 0

  @Bean(name = Array[String]("monaRestServer"))
  def monaRestServer(@Value("${mona.rest.server.host}")monaServerHost: String, @Value("${mona.rest.server.port:8080}") monaServerPort:Int ): String = {
    s"http://${monaServerHost}:${monaServerPort}"
  }

  @Bean
  def connectionManager(): HttpClientConnectionManager = {
    logger.info(s"creating connection manager for ${monaMaxConnections} max connections and ${monaMaxRouteConnections} for each route")
    val connectionManager = new PoolingHttpClientConnectionManager()
    connectionManager.setDefaultMaxPerRoute(monaMaxRouteConnections)
    connectionManager.setMaxTotal(monaMaxConnections)

    connectionManager
  }

  /**
    * rest operations interface, configured with a custom object mapper
    *
    * @return
    */
  @Bean
  def restOperations(connectionManager: HttpClientConnectionManager): RestOperations = {

    logger.info("creating rest template")

    val httpClient = HttpClientBuilder.create()
      .setConnectionManager(connectionManager)
      .build()

    val rest: RestTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient))
    rest.getMessageConverters.add(0, mappingJacksonHttpMessageConverter)
    rest
  }

  /**
    * provides us with an easy way to authenticate against the services
    *
    * @return
    */
  @Bean
  @Primary
  def loginService(@Value("${mona.rest.server.host}")monaServerHost: String, @Value("${mona.rest.server.port:8080}") monaServerPort:Int ): LoginService = new RestLoginService(monaServerHost, monaServerPort)

  /**
    * generates our mapping converter
    *
    * @return
    */
  @Bean
  def mappingJacksonHttpMessageConverter: MappingJackson2HttpMessageConverter = {
    val converter: MappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter()
    converter.setObjectMapper(MonaMapper.create)
    converter
  }


  @Bean
  def monaSpectrumRestClient: MonaSpectrumRestClient = {
    new MonaSpectrumRestClient
  }

}
