package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class SwaggerConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.application.name}")
    private String appName;


    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant("/rest/**"))
                .build().apiInfo(apiInfo());
    }

    @Bean
    public UiConfiguration uiConfig() {
        return new UiConfiguration("validatorUrl", "none", "alpha", "schema",
                UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS, false, true, 60000L);
    }


    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "Massbank of Northern America (MoNA)",
                "description of the api for the " + appName,
                "V1",
                "Terms of service",
                new Contact("Gert Wohlgemuth", "http://mona.fiehnlab.ucdavis.edu", "wohlgemuth@ucdavis.edu"),
                "GNU Lesser General Public License",
                "http://www.gnu.org/licenses/lgpl-3.0.en.html",
                Collections.emptyList());
        return apiInfo;
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                //swagger endpoint for rest documentation
                .antMatchers(HttpMethod.GET, "/v2/api-docs")
                .antMatchers(HttpMethod.GET, "/webjars/**");
    }
}