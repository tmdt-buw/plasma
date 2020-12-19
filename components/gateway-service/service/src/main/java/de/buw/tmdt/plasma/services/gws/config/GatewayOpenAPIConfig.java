package de.buw.tmdt.plasma.services.gws.config;

import org.springdoc.core.SwaggerUiConfigParameters;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class GatewayOpenAPIConfig {

    private final SwaggerUiConfigParameters swaggerUiConfigParameters;
    private final RouteDefinitionLocator locator;

    public GatewayOpenAPIConfig(SwaggerUiConfigParameters swaggerUiConfigParameters, RouteDefinitionLocator locator) {
        this.swaggerUiConfigParameters = swaggerUiConfigParameters;
        this.locator = locator;
    }

    @PostConstruct
    public void init(){
        List<RouteDefinition> definitions = locator.getRouteDefinitions().collectList().block();
        if(definitions != null){
            definitions.stream().filter(routeDefinition -> routeDefinition.getId().matches("plasma-.*")).forEach(routeDefinition
                    -> swaggerUiConfigParameters.addGroup(routeDefinition.getId()));
        }
    }
}