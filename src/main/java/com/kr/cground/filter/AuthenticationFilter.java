package com.kr.cground.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Value("${proxy.auth.url}")
    private String proxyAuthUrl;

    public static class Config {
    }

    @Override
    public GatewayFilter apply(AuthenticationFilter.Config config) {
        return (exchange, chain) -> {
            String path =  exchange.getRequest().getURI().getPath();

            if (path.contains("/public")) {
                return chain.filter(exchange);
            }

            return WebClient.builder().build()
                    .post()
                    .uri(proxyAuthUrl) // 인증 서버의 URL
                    .header("Authorization", exchange.getRequest().getHeaders().getFirst("Authorization"))
                    .retrieve()
                    .toBodilessEntity()
                    .flatMap(voidResponseEntity -> chain.filter(exchange))
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().writeWith(
                                Mono.just(exchange.getResponse().bufferFactory().wrap(ex.getResponseBodyAsString().getBytes(StandardCharsets.UTF_8))));
                    });

        };
    }
}
