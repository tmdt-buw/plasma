package de.buw.tmdt.plasma.services.dss.shared.feignclient;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients("de.buw.tmdt.plasma.services.dss.shared.feignclient")
@Configuration
public class DataSourceFeignConfiguration {
	@Bean
	public BasicAuthRequestInterceptor getDataSourceServiceAuthInterceptor(
			@Value("${plasma.services.dss.user}") String user,
			@Value("${plasma.services.dss.password}") String password
	) {
		return new BasicAuthRequestInterceptor(user, password);
	}
}