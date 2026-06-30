package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.{ClassificationCacheRestClient, MonaSpectrumRestClient}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.service.RestLoginService
import org.apache.http.client.config.RequestConfig
import org.apache.http.conn.HttpClientConnectionManager
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation._
import org.springframework.http.MediaType
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

  // Timeouts so a slow or unresponsive external service can never hang
  @Value("${mona.rest.client.timeout.connect:10000}")
  val connectTimeout: Int = 0

  @Value("${mona.rest.client.timeout.socket:30000}")
  val socketTimeout: Int = 0

  @Value("${mona.rest.client.timeout.connectionRequest:10000}")
  val connectionRequestTimeout: Int = 0

  @Bean(name = Array[String]("monaRestServer"))
  def monaRestServer(@Value("${mona.rest.server.host:localhost}") monaServerHost: String, @Value("${mona.rest.server.port:8080}") monaServerPort: Int): String = {
    logger.info(s"The rest server is http://$monaServerHost:$monaServerPort")
    s"http://$monaServerHost:$monaServerPort"
  }

  @Bean
  def connectionManager(): HttpClientConnectionManager = {
    logger.info(s"creating connection manager for $monaMaxConnections max connections and $monaMaxRouteConnections for each route")
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

    logger.info(s"creating rest template with connect=$connectTimeout, socket=$socketTimeout, connectionRequest=$connectionRequestTimeout (ms)")

    val requestConfig: RequestConfig = RequestConfig.custom()
      .setConnectTimeout(connectTimeout)
      .setSocketTimeout(socketTimeout)
      .setConnectionRequestTimeout(connectionRequestTimeout)
      .build()

    val httpClient = HttpClientBuilder.create()
      .setConnectionManager(connectionManager)
      .setDefaultRequestConfig(requestConfig)
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
  def loginService(@Value("${mona.rest.server.host:localhost}") monaServerHost: String, @Value("${mona.rest.server.port:8080}") monaServerPort: Int): LoginService = new RestLoginService(monaServerHost, monaServerPort)

  /**
    * generates our mapping converter
    *
    * @return
    */
  @Bean
  def mappingJacksonHttpMessageConverter: MappingJackson2HttpMessageConverter = {
    val converter: MappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter()
    converter.setObjectMapper(MonaMapper.create)
    // Some external services (e.g. ClassyFire's queries poll endpoint) return JSON bodies tagged as text/json,
    // which the default converter does not claim, causing the response to fail extraction. Append it so those
    // bodies still deserialize, keeping application/json first so request bodies are still written as json
    val mediaTypes = new java.util.ArrayList[MediaType](converter.getSupportedMediaTypes())
    mediaTypes.add(new MediaType("text", "json"))
    converter.setSupportedMediaTypes(mediaTypes)
    converter
  }

  @Bean
  def monaSpectrumRestClient: MonaSpectrumRestClient = {
    new MonaSpectrumRestClient
  }

  @Bean
  def classificationCacheRestClient: ClassificationCacheRestClient = {
    new ClassificationCacheRestClient
  }
}
