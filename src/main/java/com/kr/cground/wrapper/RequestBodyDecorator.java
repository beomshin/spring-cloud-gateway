package com.kr.cground.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RequestBodyDecorator extends ServerHttpRequestDecorator {

    private final ServerWebExchange exchange;


    public RequestBodyDecorator(ServerWebExchange exchange) {
        super(exchange.getRequest());
        this.exchange = exchange;
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return super.getBody().publishOn(Schedulers.boundedElastic()).doOnNext(dataBuffer -> {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                Channels.newChannel(byteArrayOutputStream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                String requestBody;

                try {
                    ObjectMapper om = new ObjectMapper();
                    JsonNode jsonNode = om.readTree(byteArrayOutputStream.toString(StandardCharsets.UTF_8));
                    requestBody = om.writeValueAsString(jsonNode);
                } catch (JsonProcessingException e) {
                    requestBody = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
                }

                log.info("Request Body: {}", requestBody);

            }  catch (Exception e) {
                log.error(e.getMessage());
            }
        });
    }
}
