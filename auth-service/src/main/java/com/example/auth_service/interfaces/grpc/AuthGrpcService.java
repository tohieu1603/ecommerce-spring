package com.example.auth_service.interfaces.grpc;

import org.springframework.grpc.server.service.GrpcService;

import com.example.auth_service.application.common.QueryHandler;
import com.example.auth_service.application.dtos.UserDTO;
import com.example.auth_service.application.port.TokenBlacklistPort;
import com.example.auth_service.application.querys.CheckRoleQuery;
import com.example.auth_service.application.querys.GetUserByIdQuery;
import com.example.auth_service.domain.services.TokenProviderPort;
import com.example.auth_service.infrastructure.security.TokenBlackList;
import com.example.auth_service.interfaces.grpc.proto.AuthServiceGrpc;
import com.example.auth_service.interfaces.grpc.proto.CheckRoleRequest;
import com.example.auth_service.interfaces.grpc.proto.CheckRoleResponse;
import com.example.auth_service.interfaces.grpc.proto.GetUserRequest;
import com.example.auth_service.interfaces.grpc.proto.GetUserResponse;
import com.example.auth_service.interfaces.grpc.proto.VerifyTokenRequest;
import com.example.auth_service.interfaces.grpc.proto.VerifyTokenResponse;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase{
    
    private final TokenBlackList tokenBlackList;
    private final TokenProviderPort tokenProviderPort;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final QueryHandler<CheckRoleQuery, Boolean> checkRoleHandler;
    private final QueryHandler<GetUserByIdQuery, UserDTO> getUserByIdHandler;

    @Override
    public void checkRole(CheckRoleRequest request, StreamObserver<CheckRoleResponse> observer) {   
        boolean ok = checkRoleHandler.handle(new CheckRoleQuery(request.getUserId(), request.getRoleName()));
        observer.onNext(CheckRoleResponse.newBuilder().setHasRole(ok).build());
        observer.onCompleted();

    }
    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> observer) {
        GetUserResponse.Builder reply = GetUserResponse.newBuilder();

        try {
            UserDTO user = getUserByIdHandler.handle(new GetUserByIdQuery(request.getUserId()));
            reply.setFound(true)
                .setUserId(nullSafe(user.id()))
                .setUsername(nullSafe(user.username()))
                .setEmail(nullSafe(user.email()))
                .setActive(user.enabled() && user.accountNonLocked()
                            && user.accountNonExpired() && user.credentialsNonExpired())
                .addAllRoles(user.roles() == null ? null : user.roles())
                .addAllPermissions(user.permissions() == null ? null : user.permissions());
                
        } catch (Exception e) {
            reply.setFound(false);
        }
        observer.onNext(reply.build());
        observer.onCompleted();
    }
    @Override
    public void verifyToken(VerifyTokenRequest request, StreamObserver<VerifyTokenResponse> observer) {
        
        VerifyTokenResponse.Builder reply = VerifyTokenResponse.newBuilder();
        try {
            var claims = tokenProviderPort.parseAccessToken(request.getAccessToken());
            if(tokenBlackList.isRevoked(claims.tokenId())) {
                reply.setValid(false);
            } else {
                reply.setValid(true)
                    .setUserId(nullSafe(claims.userId()))
                    .setUsername(nullSafe(claims.username()))
                    .setTokenVersion(claims.tokenVersion())
                    .addAllRole(claims.roles());
            }
        } catch (Exception e) {
            log.debug("gRPC verifyToken failed: {}", e.getMessage());
            reply.setValid(false);
        }

        observer.onNext(reply.build());
        observer.onCompleted();
    }

    private static String nullSafe(String s) { return s == null ? "" : s;}

}
