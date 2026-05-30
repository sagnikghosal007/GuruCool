package com.gurucool.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class LoggingFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        String requestId = request.getHeaders().getFirst("X-Request-Id");
        String userId = request.getHeaders().getFirst("X-User-Id");

        log.info("[GATEWAY-IN] requestId={} method={} path={} userId={}",
                requestId, request.getMethod(), request.getURI().getPath(), userId);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            long latency = System.currentTimeMillis() - startTime;
            log.info("[GATEWAY-OUT] requestId={} status={} latencyMs={}",
                    requestId, response.getStatusCode(), latency);
        }));
    }
}
