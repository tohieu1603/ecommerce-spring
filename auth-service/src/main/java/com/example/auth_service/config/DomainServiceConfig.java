package com.example.auth_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.auth_service.domain.services.TokenDomainService;
import com.example.auth_service.infrastructure.security.JwtProperties;

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
}
