package com.kr.cground.route;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("lawgg-web-spring", r -> r
                        .path("/lawgg/**")
                        .uri("lb://LAWGG-WEB"))
                .route("lawgg-admin-spring", r -> r
                        .path("/lawgg-admin-spring/**")
                        .uri("lb://LAWGG-ADMIN"))
                .route("formdang-api-spring", r -> r
                        .path("/formdang-spring/**")
                        .uri("lb://FORMDANG-API"))
                .route("formdang-auth-spring", r -> r
                        .path("/formdang-spring-auth/**")
                        .uri("lb://FORMDANG-AUTH"))
                .build();
    }
}
