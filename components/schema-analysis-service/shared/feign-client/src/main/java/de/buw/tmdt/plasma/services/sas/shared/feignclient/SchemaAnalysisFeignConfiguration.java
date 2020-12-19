package de.buw.tmdt.plasma.services.sas.shared.feignclient;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients("de.buw.tmdt.plasma.services.sas.shared.feignclient")
@Configuration
public class SchemaAnalysisFeignConfiguration {
	@Bean
	public BasicAuthRequestInterceptor getSchemaAnalysisServiceAuthInterceptor(
			@Value("${plasma.services.sas.user}") String user,
			@Value("${plasma.services.sas.password}") String password
	) {
		return new BasicAuthRequestInterceptor(user, password);
	}
}