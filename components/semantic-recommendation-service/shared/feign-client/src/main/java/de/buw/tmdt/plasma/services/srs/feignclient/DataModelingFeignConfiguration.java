package de.buw.tmdt.plasma.services.srs.feignclient;

import feign.auth.BasicAuthRequestInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients("de.buw.tmdt.plasma.services.srs.feignclient")
@Configuration
public class DataModelingFeignConfiguration {

	@Bean
	public CloseableHttpClient client() {
		return HttpClients.createDefault();
	}

	@Bean
	public BasicAuthRequestInterceptor getDataModelingServiceAuthInterceptor(
			@Value("${plasma.services.srs.user}") String user,
			@Value("${plasma.services.srs.password}") String password
	) {
		return new BasicAuthRequestInterceptor(user, password);
	}
}