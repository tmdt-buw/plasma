package de.buw.tmdt.plasma.services.gws.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
public class BasicAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<BasicAuthGatewayFilterFactory.Config> {

    public BasicAuthGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            String auth = config.username + ":" + config.password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes((StandardCharsets.UTF_8)));
            String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);

            ServerHttpRequest request = exchange.getRequest().mutate().header(HttpHeaders.AUTHORIZATION, authHeader).build();
            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public Config newConfig() {
        return new Config();
    }

    public static class Config {
        private String username;
        private String password;

        public Config() {
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("username",
                "password");
    }
}