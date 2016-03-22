package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.{GenericRestClient, MonaSpectrumRestClient}
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation._
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.{RestOperations, RestTemplate}

/**
  * Created by wohlg_000 on 3/2/2016.
  */
@Configuration
@Import(Array(classOf[DomainConfig]))
class RestClientConfig extends LazyLogging {

  @Value("${mona.rest.server.url}")
  val monaServerUrl: String = null

  @Bean(name = Array[String]("monaRestServer"))
  def monaRestServer: String = monaServerUrl

  @Bean
  def restOperations: RestOperations = {
    val rest: RestTemplate = new RestTemplate()
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
