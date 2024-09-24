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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("inttest")
public class FbisControllerTest {

    private static final Logger log = LoggerFactory.getLogger(FbisControllerTest.class);

    @Value("${local.server.port}")
    private int port;

    @Value("${push.notification.delay}")
    private Long pushNotificationDelay;

    private String URL;

    private static final String TOPIC_ENDPOINT_123 = "/topic/buspos/123/";
    private static final String TOPIC_ENDPOINT_456 = "/topic/buspos/456/";

    @BeforeEach
    public void beforeEach() {
        URL = "ws://localhost:" + port + "/bus-location-feed";
    }

    @Test
    void testSubscribeAndGetNotifications() throws InterruptedException {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        BlockingQueue<BusPositionResponse> blockingQueue = new ArrayBlockingQueue<>(1);
        CompletableFuture<StompSession> connecting = stompClient.connectAsync(URL, new FbisStompSessionHandler(blockingQueue));
        connecting.whenComplete((session, e) -> {
            log.info("made connection");
            session.subscribe(TOPIC_ENDPOINT_123, new FbisStompSessionHandler(blockingQueue));
        });
        await()
                .atMost(pushNotificationDelay + 1000, MILLISECONDS)
                .untilAsserted(() -> assertNotNull(blockingQueue.poll()));
    }

    @Test
    void testSubscribeTwoFeedsAndGetNotificationsFromBoth() {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        BlockingQueue<BusPositionResponse> blockingQueue = new ArrayBlockingQueue<>(2);
        CompletableFuture<StompSession> connecting = stompClient.connectAsync(URL, new FbisStompSessionHandler(blockingQueue));
        connecting.whenComplete((session, e) -> {
            log.info("made connection");
            session.subscribe(TOPIC_ENDPOINT_123, new FbisStompSessionHandler(blockingQueue));
            session.subscribe(TOPIC_ENDPOINT_456, new FbisStompSessionHandler(blockingQueue));
        });
        await()
                .atMost(pushNotificationDelay + 1000, MILLISECONDS)
                .untilAsserted(() -> this.makeAssertions(blockingQueue));
    }

    private void makeAssertions(BlockingQueue<BusPositionResponse> blockingQueue) {
        final List<BusPositionResponse> pushedResponses = new ArrayList<>();
        blockingQueue.drainTo(pushedResponses);
        assertThat(pushedResponses, hasSize(2));
        Map<String, BusPositionResponse> responseMap = pushedResponses.stream().collect(
                Collectors.toMap(BusPositionResponse::getLineRef, Function.identity()));
        assertThat(responseMap.keySet(), hasItems("123", "456"));
    }

    private static class FbisStompSessionHandler extends StompSessionHandlerAdapter {

        private final BlockingQueue<BusPositionResponse> blockingQueue;

        public FbisStompSessionHandler(BlockingQueue<BusPositionResponse> blockingQueue) {
            this.blockingQueue = blockingQueue;
        }

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
