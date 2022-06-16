package de.buw.tmdt.plasma.services.srs.feignclient;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients("de.buw.tmdt.plasma.services.srs.feignclient")
@Configuration
public class SemanticRecommendationFeignConfiguration {

}