package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@Profile("docker")
public class SwaggerConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.application.name}")
    private String appName;

    public OpenAPI monaOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("MassBank of North America (MoNA)")
                        .description("API Documentation for the " + appName)
                        .version("v2")
                        .license(new License().name("GNU Lesser General Public License").url("http://www.gnu.org/licenses/lgpl-3.0.en.html")));

    }

    @Override
    public void configure(WebSecurity web) {
        // Swagger endpoint for rest documentation
        web.ignoring()
                .antMatchers(HttpMethod.GET, "/v3/api-docs")
                .antMatchers(HttpMethod.GET, "/webjars/**");
    }
}
