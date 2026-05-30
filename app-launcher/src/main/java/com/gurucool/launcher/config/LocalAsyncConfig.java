package com.gurucool.launcher.config;

/**
 * Async config is inherited from com.gurucool.common.config.AsyncConfig.
 * Bean definition overriding is enabled via spring.main.allow-bean-definition-overriding=true
 * so services can define their own executors without conflict.
 * This class is intentionally left empty.
 */
public class LocalAsyncConfig {
    // no-op — AsyncConfig in common module provides the taskExecutor bean
}
