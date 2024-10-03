package com.kr.cground.filter;

import com.kr.cground.wrapper.RequestBodyDecorator;
import com.kr.cground.wrapper.ResponseBodyDecorator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StopWatch;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class LoggingGlobalFilter implements GlobalFilter {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        return chain.filter(
                exchange
                        .mutate()
                        .request(getRequestBodyDecorator(exchange))
                        .response(new ResponseBodyDecorator(exchange))
                        .build()
                )
                .then(Mono.fromRunnable(() -> {
                    if (exchange.getResponse().isCommitted() && isPrint(exchange.getRequest())) {
                        stopWatch.stop();
                        HttpHeaders headers = exchange.getResponse().getHeaders();
                        MediaType mediaType = headers.getContentType();
                        log.info("Returned status=[{}] in [{}]ms, mediaType=[{}]", exchange.getResponse().getStatusCode(), stopWatch.getTotalTimeMillis(),  mediaType);
                    }
                }));
    }

    private RequestBodyDecorator getRequestBodyDecorator(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();

        if (isPrint(request)) {

            String clientIp = request.getRemoteAddress().getAddress().getHostAddress();
            String realClientIp = getRealClientIp(request);

            if (clientIp != null && clientIp.equals(realClientIp)) { // IP, 메소드, URL
                log.info("Request: {} [{}] [{}]", realClientIp, request.getMethod(), request.getURI());
            } else {
                log.info("Request: {} → {} [{}] [{}]", realClientIp, clientIp, request.getMethod(), request.getURI());
            }

            printHeader(request);
            printParameter(request);


            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

            if (route != null) {
                log.info("Route routeUri: {} >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", route.getUri());
            }
        }

        return new RequestBodyDecorator(exchange);
    }

    private void  printHeader(ServerHttpRequest request) {
        for (String name : request.getHeaders().keySet()) {
            log.debug("header[{}] = {}", name, request.getHeaders().get(name));
        }
    }

    private void printParameter(ServerHttpRequest request) {
        for (String name : request.getQueryParams().keySet()) {
            log.info("parameter[{}] = {}", name, request.getQueryParams().get(name));
        }
    }

    private String getRealClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress().getAddress().getHostAddress();
        }
        return ip;
    }

    private boolean isPrint(ServerHttpRequest request) {
        if (request.getURI().getPath().contains("/static")) return false;
        return true;
    }
}
