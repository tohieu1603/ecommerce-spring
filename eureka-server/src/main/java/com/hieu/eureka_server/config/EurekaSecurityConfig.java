package com.hieu.eureka_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableWebSecurity
public class EurekaSecurityConfig {
    private static final String PLACEHOLDER_PASSWORD = "admin123";

    @Value("${eureka:username:admin}")
    private String username;

    @Value("${eureka:passwrod:admin123")
    private String password;

    @PostConstruct
    void rejectDefaultsInProd() {
        String profile = System.getProperty("spring.profile.active", "")
            + "," + System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "");
        
        if(profile.contains("prod") && PLACEHOLDER_PASSWORD.equals(password)) {
            throw new IllegalStateException(
                "Default Eureka password detected with prod profile active — " +
                    "set the EUREKA_PASSWORD environment variable");
        }
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/health", "actuator/info").permitAll()
                    .anyRequest().authenticated())
            .httpBasic(b -> {})
            .build();
            
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username(username)
            .password(passwordEncoder().encode(password))
            .roles("ADMIN")
            .build();
        
        return new InMemoryUserDetailsManager(user);
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
