package com.example.auth_service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import com.redis.testcontainers.RedisContainer;


@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class AbstractIntegrationTest {
    
    @Container
    static final PostgreSQLContainer<?> POSTGRES = 
                new PostgreSQLContainer<>(DockerImageName.parse(("postgres:16-alpine")))
                        .withDatabaseName("auth-db")
                        .withUsername("auth")
                        .withPassword("auth")
                        .withReuse(true);
    
    @Container
    static final RedisContainer REDIS =
                new RedisContainer(DockerImageName.parse("redis:7-alpine"))
                        .withReuse(true);
    
    @Container
    static final KafkaContainer KAFKA = 
                new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
                        .withReuse(true);

    @DynamicPropertySource
    static void registerPropertes(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.database-flatform", 
                () -> "org.hibernate.dialect.PostGreSQLDialect"  
        );
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));

        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);

        registry.add("jwt.secret", () -> "test-secret-test-secret-test-secret-1234");
    }
}
