package edu.ucdavis.fiehnlab.mona.app.server.proxy

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.{NegatedServerWebExchangeMatcher, ServerWebExchangeMatchers}
import org.springframework.stereotype.Controller

/**
  * Created by wohlgemuth on 3/28/16.
  */
@SpringBootApplication(exclude = Array(classOf[ReactiveSecurityAutoConfiguration], classOf[ReactiveManagementWebSecurityAutoConfiguration]))
@EnableDiscoveryClient
@Controller
@RefreshScope
class ProxyServer

object ProxyServer extends App {
  new SpringApplication(classOf[ProxyServer]).run()
}


@Configuration
@EnableWebFluxSecurity
@Order(10)
class SecurityConfiguration {
  @Bean
  def securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = {
    http.securityMatcher(new NegatedServerWebExchangeMatcher(ServerWebExchangeMatchers.pathMatchers("/**")))
      .authorizeExchange().anyExchange().authenticated()
      .and()
      .csrf().disable()
      .build()
  }
}
