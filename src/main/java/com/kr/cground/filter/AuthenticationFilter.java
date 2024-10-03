package com.kr.cground.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {


    public static class Config {
        private final String uri = "http://localhost:22081/formdang-spring-auth/validate";
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
                    .uri(config.uri) // 인증 서버의 URL
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
