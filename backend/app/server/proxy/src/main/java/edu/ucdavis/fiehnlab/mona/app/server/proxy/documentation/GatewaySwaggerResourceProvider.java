package edu.ucdavis.fiehnlab.mona.app.server.proxy.documentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.ProxyRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Primary
public class GatewaySwaggerResourceProvider implements SwaggerResourcesProvider {

    private final Logger log = LoggerFactory.getLogger(GatewaySwaggerResourceProvider.class);

    //Can be used to get the list from registry/gateway later.
    @Autowired
    private ProxyRouteLocator routeLocator;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();

        //Add the registered microservices swagger docs as additional swagger resources
        Map<String, String> routes = routeLocator.getRoutes();

        Map<String, SwaggerResource> cache = new HashMap<String, SwaggerResource>();

        routes.forEach((path, serviceId) -> {
            log.info("path: " + path + " and service " + serviceId);

            List<ServiceInstance> services = discoveryClient.getInstances(serviceId);


            if (services.size() > 0) {
                ServiceInstance serviceInstance = services.get(0);

                String location = "http://" + serviceInstance.getHost() + ":" +serviceInstance.getPort() + "/v2/api-docs";
                String newRoute = "/" + serviceId + "/v2/api-docs";

                cache.put(location, swaggerResource(serviceId, newRoute, "2.0"));

                if (routes.get(newRoute) == null) {
                    log.info("adding new route: " + newRoute + " to location " + location);
                    routeLocator.addRoute(newRoute, location);

                }
            }

        });

        resources.addAll(cache.values());
        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location, String version) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion(version);
        return swaggerResource;
    }


}