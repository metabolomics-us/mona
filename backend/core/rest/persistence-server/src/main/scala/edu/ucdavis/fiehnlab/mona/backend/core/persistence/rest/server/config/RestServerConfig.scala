package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import java.io.OutputStream
import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp.MSPWriter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.SwaggerConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.config.PersistenceServiceConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{ComponentScan, Configuration, Import}
import org.springframework.core.annotation.Order
import org.springframework.http._
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.{AbstractHttpMessageConverter, HttpMessageConverter}
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.web.servlet.config.annotation.{ContentNegotiationConfigurer, WebMvcConfigurerAdapter}

import scala.collection.JavaConverters._
import scala.collection.convert.Wrappers

/**
  * this class configures all our controller and also prepares security measures for these mentioned controllers
  */
@Configuration
@Import(Array(classOf[PersistenceServiceConfig], classOf[SwaggerConfig], classOf[SerializationConfig]))
@ComponentScan(basePackageClasses = Array(classOf[GenericRESTController[Spectrum]]))
@Order(1)
class RestServerConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  val restSecurityService: RestSecurityService = null

  /**
    * this method configures authorized access to the system
    * and protects the urls with the specified methods and credentials
    *
    * @param http
    */
  override final def configure(http: HttpSecurity): Unit = {
    restSecurityService.prepare(http)
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()

      //saves need to be authenticated
      .antMatchers(HttpMethod.POST, "/rest/spectra/**").authenticated()
      .antMatchers(HttpMethod.POST, "/rest/submitters/**").authenticated()

      //updates needs authentication
      .antMatchers(HttpMethod.PUT).authenticated()

      //deletes need authentication
      .antMatchers(HttpMethod.DELETE).hasAuthority("ADMIN")

      //update statistics need authentication
      .antMatchers(HttpMethod.POST, "/rest/statistics/update").hasAuthority("ADMIN")
  }

  /**
    * this method configures, which parts of the system and which methods do not need
    * any form of security in place and can be openly accessed
    *
    * @param web
    */
  override def configure(web: WebSecurity): Unit = {
    web.ignoring()
      //get is always available
      .antMatchers(HttpMethod.GET)
      .antMatchers(HttpMethod.POST, "/rest/spectra/count")

      //no authentication for metadata
      .antMatchers(HttpMethod.POST, "/rest/metaData/**")
      .antMatchers(HttpMethod.GET, "/*")
  }
}

@Configuration
class SerializationConfig extends WebMvcConfigurerAdapter with LazyLogging {

  override def extendMessageConverters(converters: util.List[HttpMessageConverter[_]]): Unit = {
    converters.add(new MSPConverter())
  }

  override def configureContentNegotiation(configurer: ContentNegotiationConfigurer): Unit = {
    configurer.favorPathExtension(false).
      favorParameter(true).
      parameterName("mediaType").
      ignoreAcceptHeader(false).
      useJaf(false)
      .defaultContentType(MediaType.APPLICATION_JSON)
      .mediaType("msp", MediaType.valueOf("txt/msp"))
      .mediaType("json", MediaType.APPLICATION_JSON)

    super.configureContentNegotiation(configurer)
  }
}

/**
  * converts from object to MSP
  */
class MSPConverter extends AbstractHttpMessageConverter[Any](MediaType.valueOf("text/msp")) {

  override def readInternal(clazz: Class[_ <: Any], inputMessage: HttpInputMessage): Spectrum = throw new RuntimeException("read is not supported!")

  override def canWrite(mediaType: MediaType): Boolean = {
    mediaType != null && mediaType.equals(MediaType.valueOf("text/msp"))
  }

  override def canRead(mediaType: MediaType): Boolean = false

  override def writeInternal(t: Any, outputMessage: HttpOutputMessage): Unit = {
    val writer = new MSPWriter

    val write = (x: Any) => {
      writer.write(x.asInstanceOf[Spectrum], outputMessage.getBody)
    }

    t match {
      case y:Spectrum =>
        write(y)

      case x: Iterable[_] =>
        x.foreach { y =>
          write(y)
        }

      case x: Wrappers.JIterableWrapper[_] =>
        x.foreach { y =>
          write(y)
        }

      case x: util.Collection[_] =>
        x.asScala.foreach { y =>
          write(y)
        }

      case _ =>
        logger.info(s"what the fuck is this ${t}")
    }
  }

  override def supports(clazz: Class[_]): Boolean = {
    clazz match {

      case q if q == classOf[Spectrum] => true
      case q if q == classOf[Iterable[_]] => true
      case q if q == classOf[Wrappers.JIterableWrapper[_]] => true
      case q if q == classOf[util.Collection[_]] => true

      case _ =>
        //        logger.debug(s"unknown class type provided: ${clazz}")
        false
    }
  }
}