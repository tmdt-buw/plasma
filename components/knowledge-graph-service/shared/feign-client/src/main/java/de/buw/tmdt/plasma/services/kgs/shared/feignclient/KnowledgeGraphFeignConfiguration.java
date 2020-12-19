package de.buw.tmdt.plasma.services.kgs.shared.feignclient;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients("de.buw.tmdt.plasma.services.kgs.shared.feignclient")
@Configuration
public class KnowledgeGraphFeignConfiguration {
    @Bean
    public BasicAuthRequestInterceptor getKnowledgeGraphServiceAuthInterceptor(
            @Value("${plasma.services.kgs.user}") String user,
            @Value("${plasma.services.kgs.password}") String password
    ) {
        return new BasicAuthRequestInterceptor(user, password);
    }
}