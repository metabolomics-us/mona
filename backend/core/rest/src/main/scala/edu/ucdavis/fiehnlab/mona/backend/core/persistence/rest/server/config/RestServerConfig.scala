package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.security.config.BasicRestSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.service.config.PersistenceServiceConfig
import org.springframework.context.annotation.{ComponentScan, Configuration, Import}
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.{WebSecurityConfigurerAdapter, EnableWebSecurity}

/**
  * Created by wohlg on 3/15/2016.
  */
@Configuration
@Import(Array(classOf[PersistenceServiceConfig], classOf[BasicRestSecurityConfig]))
@ComponentScan(basePackageClasses = Array(classOf[GenericRESTController[Spectrum]]))
class RestServerConfig {

}

