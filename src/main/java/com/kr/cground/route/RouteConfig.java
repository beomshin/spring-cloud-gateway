package com.kr.cground.route;

import com.kr.cground.filter.AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RouteConfig {

    private final AuthenticationFilter authenticationFilter;

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
//                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://FORMDANG-API"))
                .build();
    }
}
