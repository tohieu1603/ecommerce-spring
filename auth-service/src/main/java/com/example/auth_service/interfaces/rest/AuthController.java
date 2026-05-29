package com.example.auth_service.interfaces.rest;

import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_service.application.commands.ChangePasswordCommand;
import com.example.auth_service.application.commands.LoginCommand;
import com.example.auth_service.application.commands.LoginWithGoogleCommand;
import com.example.auth_service.application.commands.LogoutCommand;
import com.example.auth_service.application.commands.RefreshTokenCommand;
import com.example.auth_service.application.commands.RegisterUserCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.application.dtos.AuthResponseDTO;
import com.example.auth_service.domain.services.TokenProviderPort;
import com.example.auth_service.infrastructure.security.AuthUserDetails;
import com.example.auth_service.interfaces.rest.dtos.AuthMeResponse;
import com.example.auth_service.interfaces.rest.dtos.ChangePasswordRequest;
import com.example.auth_service.interfaces.rest.dtos.GoogleLoginRequest;
import com.example.auth_service.interfaces.rest.dtos.LoginRequest;
import com.example.auth_service.interfaces.rest.dtos.RegisterRequest;
import com.example.auth_service.interfaces.rest.support.AuthCookieWriter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Register, login, token rotation, logout, password change.")
@RequiredArgsConstructor
public class AuthController {
    
    private final CommandHandler<RegisterUserCommand, AuthResponseDTO> registHandler;
    private final CommandHandler<LoginCommand, AuthResponseDTO> logiHandler;
    private final CommandHandler<LoginWithGoogleCommand, AuthResponseDTO> googleLoginHandler;
    private final CommandHandler<RefreshTokenCommand, AuthResponseDTO> refreshHandler;
    private final CommandHandler<LogoutCommand, Void> logoutHandler;
    private final CommandHandler<ChangePasswordCommand, Void> changePasswordHandler;
    private final AuthCookieWriter cookieWriter;
    private final TokenProviderPort tokenProvider;


    /**
     * 
     * @param principal
     * @return 200 OK with a safe projection of the principal
     */
    @Operation(summary = "Current authenticated principal")
    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(@AuthenticationPrincipal AuthUserDetails principal) {
        return ResponseEntity.ok(AuthMeResponse.from(principal));
    }

    /**
     * Register new user account and sets auth cookies
     * @param request
     * @return Status code 200 with Set-Cookie header + user profile
     */
    @Operation(summary = "Register new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponseDTO token = registHandler.handle(request.toCommand());
        return cookieWriter.writeTokens(token);
    }
    
    /**
     * Login username/email and password -> set HttpOnly Cookie
     * @param request
     * @return Status 200 with Set-Cookie header + user profile
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        AuthResponseDTO token = logiHandler.handle(request.toCommand());
        return cookieWriter.writeTokens(token);
    }
    
    @Operation(summary = "Login with Google (ID token from Google Identity Services)")
    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> google(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResponseDTO token = googleLoginHandler.handle(new LoginWithGoogleCommand(request.idToken()));
        return cookieWriter.writeTokens(token);
    }

        /**
     * Rotates the refresh token (read from cookie) into a fresh pair.
     *
     * @param request     optional legacy body carrying {@code refreshToken}
     * @param httpRequest servlet request used to read the REFRESH_TOKEN cookie
     * @return 200 OK with new Set-Cookie headers + user profile
     */
    @Operation(summary = "Rotate refresh token (cookie-first, falls back to body)")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(HttpServletRequest request,
                                                    @RequestBody(required=false) RefreshBody body) {
        String refresh = readRefreshToken(request, body);
        AuthResponseDTO token = refreshHandler.handle(new RefreshTokenCommand(refresh));
        return cookieWriter.writeTokens(token);
    }

    
    @Operation(summary="Revoke session", security = @SecurityRequirement(name= "Bearer"))
    @PostMapping("logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String accessToken = readAccessToken(request);
        String refreshToken = readCookie(request, AuthCookieWriter.REFRESH_COOKIE);

        logoutHandler.handle(new LogoutCommand(accessToken, refreshToken));
        HttpHeaders headers = new HttpHeaders();
        cookieWriter.expire(headers);
        return ResponseEntity.noContent()
            .headers(headers)
            .build();
    }

    /**
     * Changes the authenticated user's password — invalidates every outstanding token.
     */
    @Operation(summary = "Change current user's password",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal AuthUserDetails principal,
                                               @Valid @RequestBody ChangePasswordRequest request,
                                               HttpServletRequest httpRequest) {
        String jti = null;
        Instant exp = null;
        String rawToken = readAccessToken(httpRequest);
        if(rawToken != null) {
            try {
                var claims = tokenProvider.parseAccessToken(rawToken);
                jti = claims.tokenId();
                exp = claims.expiresAt();

            } catch (Exception e) {

            }
        }
        changePasswordHandler.handle(new ChangePasswordCommand(
            principal.userId(), request.oldPassword(), request.newPassword(), jti, exp
        ));

        HttpHeaders headers = new HttpHeaders();
        cookieWriter.expire(headers);
        return ResponseEntity.noContent().headers(headers).build();
    }



    private static String readAccessToken(HttpServletRequest request) {
        String fromCookie = readCookie(request, AuthCookieWriter.ACCESS_COOKIE);
        if(fromCookie != null) return fromCookie;

        String header = request.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7).trim();

        return (token == null || token.isEmpty()) ? null : token;
    
    }

    private static String readRefreshToken(HttpServletRequest request, RefreshBody body) {
        String fromCookie = readCookie(request, AuthCookieWriter.REFRESH_COOKIE);
        if(fromCookie != null) return fromCookie;

        return body == null ? null : body.refreshToken();
    }
    private static String readCookie(HttpServletRequest requets, String name) {
        if(requets.getCookies() == null) return null;
        for(Cookie c : requets.getCookies()) {
            if(name.equals(c.getName())) {
                String value = c.getValue();
                return (value == null || value.isBlank()) ? null : value;
            }
        }
        return null;
    }

    public record RefreshBody(String refreshToken) {}

}
