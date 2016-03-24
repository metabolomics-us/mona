package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config

import java.net.Inet4Address

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.{GenericRestClient, MonaSpectrumRestClient}
import org.apache.http.HttpHost
import org.apache.http.auth.{UsernamePasswordCredentials, AuthScope}
import org.apache.http.impl.client.{HttpClients, BasicCredentialsProvider}
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation._
import org.springframework.http.client.{HttpComponentsClientHttpRequestFactory, ClientHttpRequestFactory}
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.{RestOperations, RestTemplate}

/**
  * Created by wohlg_000 on 3/2/2016.
  */
@Configuration
@Import(Array(classOf[DomainConfig]))
class RestClientConfig extends LazyLogging {

  @Value("${mona.rest.server.host}")
  val monaServerHost: String = null


  @Value("${mona.rest.server.port}")
  val monaServerPort: Int = 0

  @Value("${mona.rest.server.user}")
  val userName: String = null

  @Value("${mona.rest.server.password}")
  val password: String = null

  @Bean(name = Array[String]("monaRestServer"))
  def monaRestServer: String = s"http://${monaServerHost}:${monaServerPort}"

  @Bean
  def requestFactory() : HttpComponentsClientHttpRequestFactory = {
    val host = new HttpHost(monaServerHost,monaServerPort)

    logger.debug(s"creating credentials for ${host.getHostName} and ${host.getPort}")
    val credentials = new BasicCredentialsProvider()
    credentials.setCredentials(new AuthScope(host.getHostName,host.getPort),new UsernamePasswordCredentials(userName,password))

    val client = HttpClients.custom().setDefaultCredentialsProvider(credentials).build()

    new HttpComponentsClientHttpRequestFactory(client)
  }

  @Bean
  def restOperations(factory:HttpComponentsClientHttpRequestFactory) : RestOperations = {
    val rest: RestTemplate = new RestTemplate(factory)
    rest.getMessageConverters.add(0, mappingJacksonHttpMessageConverter)
    rest
  }

  @Bean
  def mappingJacksonHttpMessageConverter: MappingJackson2HttpMessageConverter = {
    val converter: MappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter()
    converter.setObjectMapper(MonaMapper.create)
    converter
  }

  @Bean
  def spectrumRestClient: GenericRestClient[Spectrum, String] = {
    new GenericRestClient[Spectrum, String]("rest/spectra")
  }

  @Bean
  def monaSpectrumRestClient:MonaSpectrumRestClient = {
    new MonaSpectrumRestClient
  }

}
