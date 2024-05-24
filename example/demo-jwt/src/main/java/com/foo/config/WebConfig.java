package com.foo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.pattern.PathPatternParser;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.foo.myapp"})
public class WebConfig implements WebMvcConfigurer {
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("/resources/static/");
	}

	
	@Bean
	public StandardServletMultipartResolver multipartResolver() {
		
		StandardServletMultipartResolver multipartResolover = new StandardServletMultipartResolver();
		multipartResolover.setStrictServletCompliance(true);
		return multipartResolover;
	}
	
	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {		
		configurer.setPatternParser(new PathPatternParser());
	}
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/guest/**").allowedOrigins("*");
	}

}
