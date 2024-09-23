package com.flatshire.fbis.components;

import com.flatshire.fbis.messages.BusPositionResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("inttest")
public class FbisControllerTest {

    private static final Logger log = LoggerFactory.getLogger(FbisControllerTest.class);

    @Value("${local.server.port}")
    private int port;
    private String URL;

    private static final String TEST_SUBSCRIBE_ENDPOINT = "/topic/buspos/123/";
    private static final BlockingQueue<BusPositionResponse> blockingQueue = new ArrayBlockingQueue<>(1);

    @BeforeEach
    public void beforeEach() {
        URL = "ws://localhost:" + port + "/bus-location-feed";
        blockingQueue.clear();
    }

    @Test
    void testSubscribe() throws InterruptedException {

        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        CompletableFuture<StompSession> connecting = stompClient.connectAsync(URL, new FbisStompSessionHandler());
        connecting.whenComplete((session, e) -> {
            log.info("made connection");
            session.subscribe(TEST_SUBSCRIBE_ENDPOINT, new FbisStompSessionHandler());
        });

        await()
                .atMost(15, SECONDS)
                .untilAsserted(() -> assertNotNull(blockingQueue.poll()));
    }

    private static class FbisStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            log.info("Yay! I'm connected!");
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            log.error("exception thrown: " + ExceptionUtils.getStackTrace(exception));
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            log.error("transport error thrown: " + exception.getMessage());
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return BusPositionResponse.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            headers.forEach((key, value) -> log.info("entry %s -> %s".formatted(key, value)));
            if(payload != null) {
                BusPositionResponse p = (BusPositionResponse) payload;
                log.info("Received response for bus " + p);
                blockingQueue.add(p);
            } else {
                log.info("payload was null");
            }
        }
    }
}
