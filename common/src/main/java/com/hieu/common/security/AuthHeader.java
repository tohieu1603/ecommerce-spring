package com.hieu.common.security;

public class AuthHeader {
    private AuthHeader() {}

    public static final String USER_ID       = "X-User-Id";
    public static final String USERNAME      = "X-User-Name";
    public static final String ROLES         = "X-User-Roles";
    public static final String TOKEN_ID      = "X-Token-Id";
    public static final String TOKEN_VERSION = "X-Token-Version";
    public static final String CORRELATION_ID = "X-Correlation-Id";
}
