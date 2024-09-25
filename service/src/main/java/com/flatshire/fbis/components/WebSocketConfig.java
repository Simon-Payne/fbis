package com.flatshire.fbis.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/bus-location-feed");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new LoggingChannelInterceptor());
    }

    private static class LoggingChannelInterceptor implements ExecutorChannelInterceptor {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            StompCommand command = accessor.getCommand();
            assert command != null;
            SimpMessageType messageType = command.getMessageType();
            if (messageType.equals(SimpMessageType.SUBSCRIBE) && message.getHeaders().containsKey("simpDestination")) {
                String topic = (String) message.getHeaders().get("simpDestination");
                log.info("Intercepted SUBSCRIBE to %s".formatted(topic));
            } else if (messageType.equals(SimpMessageType.UNSUBSCRIBE)) {
                List<String> destinations = accessor.getNativeHeader("destination");
                if (destinations != null && !destinations.isEmpty()) {
                    String topic = destinations.get(0);
                    log.info("Intercepted UNSUBSCRIBE from %s".formatted(topic));
                }
            } else if (messageType.equals(SimpMessageType.MESSAGE)) {
                byte[] payload = (byte[]) message.getPayload();
                log.info("Message payload: %s".formatted(new String(payload, StandardCharsets.UTF_8)));
            } else {
                log.info("Intercepted message type %s".formatted(messageType.name()));
            }
            return message;
        }
    }
}