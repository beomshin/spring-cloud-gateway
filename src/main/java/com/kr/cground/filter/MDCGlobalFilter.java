package com.kr.cground.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCGlobalFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerWebExchange mutatedExchange = exchange;

        if (StringUtils.isBlank(exchange.getRequest().getHeaders().getFirst("X-Request-ID"))) { // MDC 값이 없는 경우 재설정해서 처리
            // 새로운 요청을 생성하여 헤더 수정
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header("X-RequestID", UUID.randomUUID().toString().replaceAll("-", ""))
                    .build();

            mutatedExchange = exchange.mutate().request(request).build();
        }

        MDC.put("request_id", mutatedExchange.getRequest().getHeaders().getFirst("X-RequestID"));
        return chain.filter(mutatedExchange).doFinally(s -> MDC.remove("request_id"));
    }

}
