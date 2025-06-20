package com.flatshire.fbis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flatshire.fbis.messages.BusPositionResponse;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
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
@AutoConfigureMockMvc
class FbisWebSocketControllerTest {

    private static final Logger log = LoggerFactory.getLogger(FbisWebSocketControllerTest.class);

    @LocalServerPort
    private Integer port;

    @Value("${push.notification.delay}")
    private Long pushNotificationDelay;

    @Value("${test.user}")
    private String testUser;

    @Value("${test.user.password}")
    private String testUserPassword;

    @Autowired
    private ObjectMapper objectMapper;

    private MessageConverter messageConverter;

    private String URL;

    private static final String TOPIC_ENDPOINT_123 = "/topic/buspos/123/";
    private static final String TOPIC_ENDPOINT_456 = "/topic/buspos/456/";

    private String base64string;

    @PostConstruct
    void postConstruct() {
        String s = testUser + ":" + testUserPassword;
        byte[] bytes = (s).getBytes(StandardCharsets.UTF_8);
        String encoding = new String(Base64.getEncoder().encode(bytes), StandardCharsets.UTF_8);
        base64string = "Basic %s".formatted(encoding);
        messageConverter = new MappingJackson2MessageConverter(objectMapper);
    }

    @BeforeEach
    public void beforeEach() {
        URL = "wss://localhost:" + port + "/bus-location-feed";
        System.setProperty("server.port", String.valueOf(port));
    }

    @Test
    void subscribeOneFeedShouldGetNotifications() throws InterruptedException {
        WebSocketClient client = configureClientProperties();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(messageConverter);
        BlockingQueue<BusPositionResponse> blockingQueue = new ArrayBlockingQueue<>(1);
        CompletableFuture<StompSession> connecting = stompClient.connectAsync(URL, new StompSessionHandlerAdapter() {
        });
        connecting.whenComplete((session, e) -> {
            log.info("made connection");
            session.subscribe(TOPIC_ENDPOINT_123, new ResponseCollectingStompSessionHandler(blockingQueue));
        });
        await()
                .atMost(pushNotificationDelay + 1000, MILLISECONDS)
                .untilAsserted(() -> assertNotNull(blockingQueue.poll()));
    }

    @Test
    void subscribeTwoFeedsShouldGetNotificationsFromBoth() {
        WebSocketClient client = configureClientProperties();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(messageConverter);
        BlockingQueue<BusPositionResponse> blockingQueue = new ArrayBlockingQueue<>(2);
        CompletableFuture<StompSession> connecting = stompClient.connectAsync(URL, new StompSessionHandlerAdapter() {
        });
        connecting.whenComplete((session, e) -> {
            log.info("made connection");
            session.subscribe(TOPIC_ENDPOINT_123, new ResponseCollectingStompSessionHandler(blockingQueue));
            session.subscribe(TOPIC_ENDPOINT_456, new ResponseCollectingStompSessionHandler(blockingQueue));
        });
        await()
                .atMost(pushNotificationDelay + 1000, MILLISECONDS)
                .untilAsserted(() -> this.assertBothTopicsMessaged(blockingQueue));
    }

    @Test
    void subscribeThenUnsubscribeShouldStopNotifications() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketClient client = configureClientProperties();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(messageConverter);
        BlockingQueue<BusPositionResponse> blockingQueue = new ArrayBlockingQueue<>(1);
        CompletableFuture<StompSession> connecting = stompClient.connectAsync(URL, new StompSessionHandlerAdapter() {
        });
        AtomicReference<StompSession.Subscription> refOfSubToUnsubscribe = new AtomicReference<>();
        connecting.whenComplete((session, e) -> {
            log.info("made connection");
            session.subscribe(TOPIC_ENDPOINT_123, new ResponseCollectingStompSessionHandler(blockingQueue));
            // store reference to second topic
            refOfSubToUnsubscribe.set(session.subscribe(TOPIC_ENDPOINT_456, new ResponseCollectingStompSessionHandler(blockingQueue)));
        });
        await()
                .atMost(pushNotificationDelay + 1000, MILLISECONDS)
                .untilAsserted(() -> assertNotNull(blockingQueue.poll()));
        // unsubscribe from second topic
        StompSession.Subscription subscription = refOfSubToUnsubscribe.get();
        subscription.unsubscribe();
        await()
                .atMost((pushNotificationDelay * 3), MILLISECONDS)
                .untilAsserted(() -> assertOneTopicMessaged(blockingQueue));
    }

    private WebSocketClient configureClientProperties() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        client.getUserProperties().put("Authorization", base64string);
        return client;
    }

    private void assertOneTopicMessaged(BlockingQueue<BusPositionResponse> blockingQueue) {
        final List<BusPositionResponse> pushedResponses = new ArrayList<>();
        blockingQueue.drainTo(pushedResponses);
        assertThat(pushedResponses, hasSize(1));
        Map<String, BusPositionResponse> responseMap = pushedResponses.stream().collect(
                Collectors.toMap(BusPositionResponse::getLineRef, Function.identity()));
        assertThat(responseMap.keySet(), hasItems("123"));
    }

    private void assertBothTopicsMessaged(BlockingQueue<BusPositionResponse> blockingQueue) {
        final List<BusPositionResponse> pushedResponses = new ArrayList<>();
        blockingQueue.drainTo(pushedResponses);
        assertThat(pushedResponses, hasSize(2));
        Map<String, BusPositionResponse> responseMap = pushedResponses.stream().collect(
                Collectors.toMap(BusPositionResponse::getLineRef, Function.identity()));
        assertThat(responseMap.keySet(), hasItems("123", "456"));
    }

    private static class ResponseCollectingStompSessionHandler extends StompSessionHandlerAdapter {

        private final BlockingQueue<BusPositionResponse> blockingQueue;

        public ResponseCollectingStompSessionHandler(BlockingQueue<BusPositionResponse> blockingQueue) {
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
            if (payload != null) {
                BusPositionResponse p = (BusPositionResponse) payload;
                log.info("Received response for bus " + p);
                blockingQueue.add(p);
            } else {
                log.info("payload was null");
            }
        }
    }
}
