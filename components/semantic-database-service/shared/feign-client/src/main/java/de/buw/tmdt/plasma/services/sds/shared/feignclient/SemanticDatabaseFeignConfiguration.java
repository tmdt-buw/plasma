package de.buw.tmdt.plasma.services.sds.shared.feignclient;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients("de.buw.tmdt.plasma.services.sds.shared.feignclient")
@Configuration
public class SemanticDatabaseFeignConfiguration {
    @Bean
    public BasicAuthRequestInterceptor getSemanticDatabaseServiceAuthInterceptor(
            @Value("${plasma.services.sds.user}") String user,
            @Value("${plasma.services.sds.password}") String password
    ) {
        return new BasicAuthRequestInterceptor(user, password);
    }
}