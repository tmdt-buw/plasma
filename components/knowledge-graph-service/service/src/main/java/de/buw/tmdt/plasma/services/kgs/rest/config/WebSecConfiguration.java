package de.buw.tmdt.plasma.services.kgs.rest.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@SuppressFBWarnings(value = "SPRING_CSRF_PROTECTION_DISABLED", justification = "only internal communication - no access from outside")
public class WebSecConfiguration extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(@NotNull final HttpSecurity http) throws Exception {
		http.csrf().disable()
				.authorizeRequests()
				.anyRequest().authenticated()
				.and().httpBasic();
	}
}