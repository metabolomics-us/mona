package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest

import java.util.Collections

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration, Profile}
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import springfox.documentation.builders.{PathSelectors, RequestHandlerSelectors}
import springfox.documentation.service.{ApiInfo, Contact}
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.UiConfiguration
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
@Profile(Array("docker"))
class SwaggerConfig extends WebSecurityConfigurerAdapter {

  @Value("${spring.application.name}")
  private val appName = null

  @Bean
  def api: Docket = {
    new Docket(DocumentationType.SWAGGER_2)
      .select
      .apis(RequestHandlerSelectors.any)
      .paths(PathSelectors.ant("/rest/**"))
      .build.apiInfo(apiInfo)
  }

  @Bean
  def uiConfig: UiConfiguration = {
    new UiConfiguration("validatorUrl", "none", "alpha", "schema", UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS, false, true, 60000L)
  }

  private def apiInfo: ApiInfo = {
    new ApiInfo(
      "MassBank of North America (MoNA)",
      s"API Documentation for the $appName",
      "v1",
      "Terms of Service",
      new Contact("MoNA Development Team", "http://mona.fiehnlab.ucdavis.edu", "wohlgemuth@ucdavis.edu"),
      "GNU Lesser General Public License",
      "http://www.gnu.org/licenses/lgpl-3.0.en.html",
      Collections.emptyList())
  }

  override def configure(web: WebSecurity): Unit = {
    // Swagger endpoint for rest documentation
    web.ignoring
      .antMatchers(HttpMethod.GET, "/v2/api-docs")
      .antMatchers(HttpMethod.GET, "/webjars/**")
  }
}
