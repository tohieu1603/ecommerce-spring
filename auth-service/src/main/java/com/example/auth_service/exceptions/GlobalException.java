package com.example.auth_service.exceptions;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.example.auth_service.domain.models.token.exceptions.TokenExpiredException;
import com.example.auth_service.domain.models.token.exceptions.TokenOwnershipException;
import com.example.auth_service.domain.models.token.exceptions.TokenReuseDetectedException;
import com.example.auth_service.domain.models.token.exceptions.TokenRevokedException;
import com.example.auth_service.domain.models.user.exceptions.AccountNotUsableException;
import com.example.auth_service.domain.models.user.exceptions.InvalidCredentialsException;
import com.example.auth_service.domain.models.user.exceptions.UserAlreadyExistsException;
import com.example.auth_service.domain.models.user.exceptions.UserNotFoundException;
import com.example.auth_service.domain.shared.DomainException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;


/**
 * Central exceptions funnel through a single handler: HTTP status is derived from the 
 * exception type via pattern matching, and the stable {@link DomainException#code()}
 * is surfaced in the response so clients can branch without parsing message.
 */
@RestControllerAdvice
@Slf4j
public class GlobalException {
    
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex, WebRequest request) {
        HttpStatus status = switch (ex) {
            case UserNotFoundException ignored          -> HttpStatus.NOT_FOUND;
            case UserAlreadyExistsException ignored     -> HttpStatus.CONFLICT;
            case InvalidCredentialsException ignored    -> HttpStatus.UNAUTHORIZED;
            case TokenExpiredException ignored          -> HttpStatus.UNAUTHORIZED;
            case TokenRevokedException ignored          -> HttpStatus.UNAUTHORIZED;
            case TokenReuseDetectedException ignored    -> HttpStatus.UNAUTHORIZED;
            case TokenOwnershipException ignored        -> HttpStatus.FORBIDDEN;
            case AccountNotUsableException ignored      -> HttpStatus.FORBIDDEN;
            default                                     -> HttpStatus.BAD_REQUEST;
        };
        log.warn("{} [{}]: {}", ex.getClass().getSimpleName(), ex.code(), ex.getMessage());
        return buildResponse(status, ex.code(), ex.getMessage(), request);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());
        
        List<ErrorResponse.ValidationError> fields = ex.getBindingResult().getAllErrors().stream()
            .map(e -> ErrorResponse.ValidationError.builder()
                .field(((FieldError) e).getField())
                .message(e.getDefaultMessage())
                .build())
            .toList();

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("validation Failed")
                .path(pathOf(request))
                .validationErrors(fields)
                .build();
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);

    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "AUTH-1001", "Invalid username or password", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> hanldeAuth(AuthenticationException ex, WebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "AUTH-1002", "Authentication failed", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "AUTH-1003", "You don't have permission access resource", request);
    }

     @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtExpired(ExpiredJwtException ex, WebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "AUTH-1004", "JWT token has expired", request);
    }

    @ExceptionHandler({SignatureException.class, MalformedJwtException.class})
    public ResponseEntity<ErrorResponse> handleJwt(Exception ex, WebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "AUTH-1005", "Invalid JWT token", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code,
                                                        String message, WebRequest request) {
        ErrorResponse body = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(status.value())
        .error(code)
        .message(message)
        .path(pathOf(request))
        .build();
    
        return new ResponseEntity<>(body, status);
    }

    private static String pathOf(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
