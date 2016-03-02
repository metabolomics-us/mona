package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.GenericRestClient
import org.springframework.context.annotation.{Configuration, Bean}
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.{RestOperations, RestTemplate}

/**
  * Created by wohlg_000 on 3/2/2016.
  */
@Configuration
class RestClientConfig {

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
  def monaServerUrl = s"http://localhost:8080/rest/spectra"

  @Bean
  def spectrumRestClient:GenericRestClient[Spectrum,String] = {
    new GenericRestClient[Spectrum,String](monaServerUrl)
  }
}
