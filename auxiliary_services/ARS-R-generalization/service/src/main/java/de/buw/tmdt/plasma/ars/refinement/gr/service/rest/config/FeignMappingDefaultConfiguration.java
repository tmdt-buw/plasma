package de.buw.tmdt.plasma.ars.refinement.gr.service.rest.config;

import feign.Feign;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

// THIS IS AN SPRING ISSUE, using feign will cause
// This fix might be removed if it is fixed by Spring/Feign itself
/// https://github.com/spring-projects/spring-framework/issues/22154
/// https://github.com/spring-cloud/spring-cloud-netflix/issues/466

@Configuration
@ConditionalOnClass({Feign.class})
public class FeignMappingDefaultConfiguration {
	@Bean
	public WebMvcRegistrations feignWebRegistrations() {
		return new WebMvcRegistrations() {
			@Override
			public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
				return new FeignFilterRequestMappingHandlerMapping();
			}
		};
	}

	private static class FeignFilterRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
		@Override
		protected boolean isHandler(@NotNull Class<?> beanType) {
			return super.isHandler(beanType) && (AnnotationUtils.findAnnotation(beanType, FeignClient.class) == null);
		}
	}
}