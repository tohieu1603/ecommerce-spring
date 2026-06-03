package com.example.auth_service.infrastructure.security;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.example.auth_service.domain.services.GoogleIdTokenVerifierPort;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class GoogleIdTokenVerifierAdapter implements GoogleIdTokenVerifierPort {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public GoogleClaims verify(String rawToken) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + rawToken;
        try {
            GoogleTokenInfo info = restTemplate.getForObject(url, GoogleTokenInfo.class);
            if (info == null || info.sub == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }
            return new GoogleClaims(
                info.sub,
                info.email,
                Boolean.parseBoolean(info.emailVerified),
                info.name,
                info.givenName,
                info.familyName,
                info.picture
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to verify Google ID token", e);
        }
    }

    private static class GoogleTokenInfo {
        public String sub;
        public String email;
        @JsonProperty("email_verified")
        public String emailVerified;
        public String name;
        @JsonProperty("given_name")
        public String givenName;
        @JsonProperty("family_name")
        public String familyName;
        public String picture;
    }
}
