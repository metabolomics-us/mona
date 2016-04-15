package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import edu.ucdavis.fiehnlab.mona.backend.core.auth.service.RestSecurityService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.config.PersistenceServiceConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{ComponentScan, Configuration, Import}
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy



/**
  * this class configures all our controller and also prepares security measures for these mentioned controllers
  */
@Configuration
@Import(Array(classOf[PersistenceServiceConfig]))
@ComponentScan(basePackageClasses = Array(classOf[GenericRESTController[Spectrum]]))
class RestServerConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  val restSecurityService:RestSecurityService = null

  /**
    * this method configures authorized access to the system
    * and protects the urls with the specified methods and credentials
    * @param http
    */
  override final def configure(http: HttpSecurity): Unit = {
    restSecurityService.prepare(http)
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()
      //saves need to be authentifiated
      .antMatchers(HttpMethod.POST, "/rest/spectra/**").authenticated()
      .antMatchers(HttpMethod.POST, "/rest/submitters/**").authenticated()

      //updates needs authentication
      .antMatchers(HttpMethod.PUT).authenticated()
      //deletes need authentication
      .antMatchers(HttpMethod.DELETE).hasAuthority("ADMIN")

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
