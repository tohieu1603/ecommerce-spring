package com.example.auth_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.auth_service.domain.services.TokenDomainService;
import com.example.auth_service.infrastructure.security.JwtProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Registers framework-free domain services as Spring beans.
 *
 * <p>The services themselves live in {@code domain/services/} and depend on nothing
 * Spring-related; this configuration is the only place where Spring glues them to
 * the application context. {@link JwtProperties} is also enabled here via
 * {@link EnableConfigurationProperties} so the {@code jwt.*} section of
 * {@code application.yaml} is bound eagerly.
 */

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class DomainServiceConfig {

    @Bean
    public TokenDomainService tokenDomainService() {
        return new TokenDomainService();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
