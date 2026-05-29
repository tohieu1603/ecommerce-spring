package com.example.auth_service.domain.services;


/**
 * Outbound port for Google Id token verified
 * <p>Infrastructure supplies the adapter (Google's officials google-api-client library)
 * Keep the port in {@code domain.services} let the google login
 */
public interface GoogleIdTokenVerifierPort {
    
    /** Validates the signature, expiry and an audience of a Google-issued ID token
     * (the {@code credential} return by Google Identity Service on the FE)
     */
    GoogleClaims verify(String rawToken);

    record GoogleClaims(
        String sub,
        String email,
        boolean emailVerified,
        String name,
        String givenName,
        String familyName,
        String picture
    ) {}
}
