package com.gurucool.common.constants;

public final class AppConstants {
    private AppConstants() {}

    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_IDEMPOTENCY_KEY = "X-Idempotency-Key";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String BEARER_PREFIX = "Bearer ";
}
