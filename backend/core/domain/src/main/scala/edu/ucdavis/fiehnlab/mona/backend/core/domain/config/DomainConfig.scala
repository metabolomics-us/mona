package edu.ucdavis.fiehnlab.mona.backend.core.domain.config

import javax.validation.{Validation, Validator}

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.springframework.context.annotation.{Bean, Configuration, Primary}
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
  * Created by wohlgemuth on 3/10/16.
  */
@Configuration
class DomainConfig {

  @Bean
  def validator: Validator = Validation.buildDefaultValidatorFactory().getValidator

  @Bean
  @Primary
  def objectMapper: ObjectMapper = MonaMapper.create

  @Bean
  @Primary
  def objectMapperBuilder: Jackson2ObjectMapperBuilder = {
    val builder = new Jackson2ObjectMapperBuilder()
    builder.serializationInclusion(JsonInclude.Include.NON_NULL)
    builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    builder.featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    builder.modules(DefaultScalaModule)
    builder
  }
}
