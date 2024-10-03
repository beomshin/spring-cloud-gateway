package com.kr.cground.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
public class ResponseBodyDecorator extends ServerHttpResponseDecorator {

    private final ServerWebExchange exchange;

    public ResponseBodyDecorator(ServerWebExchange exchange) {
        super(exchange.getResponse());
        this.exchange = exchange;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        MediaType contentType = exchange.getResponse().getHeaders().getContentType();

        if (body instanceof Flux<? extends DataBuffer> b && MediaType.APPLICATION_JSON.includes(contentType)) {

            return super.writeWith(b.buffer().map(dataBuffers -> {

                DefaultDataBuffer joinedBuffers = new DefaultDataBufferFactory().join(dataBuffers);
                byte[] content = new byte[joinedBuffers.readableByteCount()];
                joinedBuffers.read(content);

                log.info("Response body: {}", new String(content, StandardCharsets.UTF_8));

                return exchange.getResponse().bufferFactory().wrap(content);
            })).onErrorResume(err -> {
                log.error(err.getMessage());
                return Mono.empty();
            });
        }
        return super.writeWith(body);
    }
}
