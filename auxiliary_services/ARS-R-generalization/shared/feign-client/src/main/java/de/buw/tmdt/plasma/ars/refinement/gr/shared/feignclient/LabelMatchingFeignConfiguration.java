package de.buw.tmdt.plasma.ars.refinement.gr.shared.feignclient;

import feign.auth.BasicAuthRequestInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients("de.buw.tmdt.plasma.ars.refinement.gr.shared.feignclient")
@Configuration
public class LabelMatchingFeignConfiguration {

	@Bean
	public CloseableHttpClient client() {
		return HttpClients.createDefault();
	}

	@Bean
	public BasicAuthRequestInterceptor getDataModelingServiceAuthInterceptor(
			@Value("${plasma.ars.refinement.gr.user}") String user,
			@Value("${plasma.ars.refinement.gr.password}") String password
	) {
		return new BasicAuthRequestInterceptor(user, password);
	}
}