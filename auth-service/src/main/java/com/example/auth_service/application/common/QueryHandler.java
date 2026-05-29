package com.example.auth_service.application.common;

public interface QueryHandler<Q extends Query<R>, R> {
    R handle(Q query);
    
}
