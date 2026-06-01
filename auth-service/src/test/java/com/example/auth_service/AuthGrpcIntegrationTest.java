package com.example.auth_service;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.auth_service.application.commands.RegisterUserCommand;
import com.example.auth_service.application.common.CommandHandler;
import com.example.auth_service.application.dtos.AuthResponseDTO;
import com.example.auth_service.application.port.TokenBlacklistPort;
import com.example.auth_service.domain.services.TokenProviderPort;
import com.example.auth_service.interfaces.grpc.AuthGrpcService;
import com.example.auth_service.interfaces.grpc.proto.CheckRoleRequest;
import com.example.auth_service.interfaces.grpc.proto.GetUserRequest;
import com.example.auth_service.interfaces.grpc.proto.GetUserResponse;
import com.example.auth_service.interfaces.grpc.proto.VerifyTokenRequest;
import com.example.auth_service.interfaces.grpc.proto.VerifyTokenResponse;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AuthGrpcIntegrationTest extends AbstractIntegrationTest{
    private final AuthGrpcService authGrpcService;
    private final CommandHandler<RegisterUserCommand, AuthResponseDTO> registerHandler;
    private final TokenProviderPort tokenProviderPort;
    private final TokenBlacklistPort tokenBlacklistPort;


    @Test
    @DisplayName("Verify token khi register success")
    void verifyToken_returnsValidTrueForFreshJwt() {
        AuthResponseDTO registered = registerHandler.handle(new RegisterUserCommand(
            "grpcuser", "tthieu160304@gmail.com", "hieu1603", "Hieu", "To"
        ));

        VerifyTokenResponse response = callVerify(registered.accessToken());

        assertThat(response.getValid()).isTrue();
        assertThat(response.getUsername()).isEqualTo("grpcuser");
        assertThat(response.getUserId()).isEqualTo(registered.user().id());
        assertThat(response.getRoleList()).isEqualTo("ROLE_USER");

    }

    @Test
    void verifyToken_returnsFalseWhenTokenBlacklisted() {
        AuthResponseDTO registered = registerHandler.handle(new RegisterUserCommand(
            "blacklisted", "hieu@gmail.com", "Hieu1603", "Hieu", "To"
        ));
        var claims = tokenProviderPort.parseAccessToken(registered.accessToken());
        tokenBlacklistPort.revoke(claims.tokenId(), claims.userId(), 
                    Instant.now().plusSeconds(900), "LOGOUT");

        VerifyTokenResponse response = callVerify(registered.accessToken());
        assertThat(response.getValid()).isFalse();
    }

    @Test
    void verifyToken_returnsFalseForGarbageToken() {
        VerifyTokenResponse response = callVerify("hieu-jwt");
        assertThat(response.getValid()).isFalse();
    }

    @Test
    void checkRole_returnsTrueForAssignedRole() {
        AuthResponseDTO registered = registerHandler.handle(new RegisterUserCommand(
            "roleuser", "hieu@gmail.com", "Hieu1603", "H", "T"
        ));

        AtomicReference<Boolean> hasRole = new AtomicReference<>();
        authGrpcService.checkRole(
            CheckRoleRequest.newBuilder()
                .setUserId(registered.user().id())
                .setRoleName("ROLE_USER")
                .build(),
            capture(r -> hasRole.set(r.getHasRole())));
       
        assertThat(hasRole.get()).isTrue();
    }

    /** Test getUser when user id is unknown */
    @Test
    void getUser_foundFalseForIdUnknown() {
        var captured = new AtomicReference<GetUserResponse>();
        authGrpcService.getUser(
            GetUserRequest.newBuilder()
                .setUserId("unknown-id")
                .build(),
            capture(captured::set)
        );
    }



    private VerifyTokenResponse callVerify(String accessToken) {
        AtomicReference<VerifyTokenResponse> holder = new AtomicReference<VerifyTokenResponse>();
        authGrpcService.verifyToken(VerifyTokenRequest.newBuilder().setAccessToken(accessToken).build(), capture(holder::set));

        return holder.get();
    }

    private static <T> StreamObserver<T> capture(Consumer<T> sink) {
        return new StreamObserver<T>() {
            @Override public void onNext(T value) { sink.accept(value); }
            @Override public void onError(Throwable t) { throw new AssertionError(t); }
            @Override public void onCompleted() {}
        };
    } 
}
