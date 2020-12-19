package de.buw.tmdt.plasma.services.kgs.rest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Value("${plasma.gateway_address}")
	private String gatewayServer;

	@Value("${plasma.server_address}")
	private String realServer;

	private final BuildProperties buildProperties;

	public OpenApiConfig(@NotNull BuildProperties buildProperties) {
		this.buildProperties = buildProperties;
	}

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.components(new Components())
				.info(new Info().title(buildProperties.get("title"))
						      .description(buildProperties.get("description"))
						      .version(buildProperties.getVersion())
						      .contact(new Contact().name(buildProperties.get("developerName")).email(buildProperties.get("developerEMail"))))
				.addServersItem(new Server().url(gatewayServer))
				.addServersItem(new Server().url(realServer));
	}

}
