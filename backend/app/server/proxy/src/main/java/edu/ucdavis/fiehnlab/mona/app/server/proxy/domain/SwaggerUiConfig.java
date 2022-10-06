package edu.ucdavis.fiehnlab.mona.app.server.proxy.domain;

import org.springdoc.core.AbstractSwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springdoc.core.AbstractSwaggerUiConfigProperties.SwaggerUrl;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
public class SwaggerUiConfig {
  @Autowired
  private DiscoveryClient discoveryClient;

  @GetMapping("/swagger-config.json")
  public Map<String, Object> swaggerConfig() {
    Map<String, Object> config = new LinkedHashMap<>();
    List<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new LinkedList<>();
    discoveryClient.getServices().forEach(serviceName ->
      discoveryClient.getInstances(serviceName).forEach(serviceInstance -> {
          SwaggerUrl swaggerUrl = new SwaggerUrl("mona", serviceInstance.getUri() + "/v3/api-docs", serviceName);
          urls.add(swaggerUrl);
        }
      )
    );
    config.put("urls", urls);
    return config;
  }
}
