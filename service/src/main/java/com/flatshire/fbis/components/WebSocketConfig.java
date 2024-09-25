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
            if (messageType.equals(SimpMessageType.SUBSCRIBE)) {
                if(message.getHeaders().containsKey("simpDestination") &&
                    message.getHeaders().containsKey("simpSubscriptionId")) {
                    String topic = (String) message.getHeaders().get("simpDestination");
                    String id = (String) message.getHeaders().get("simpSubscriptionId");
                    log.info("Intercepted SUBSCRIBE to %s of subscription id %s".formatted(topic, id));
                }
            } else if (messageType.equals(SimpMessageType.UNSUBSCRIBE)) {
                List<String> subscriptionIds = accessor.getNativeHeader("id");
                if (subscriptionIds != null && !subscriptionIds.isEmpty()) {
                    log.info("Intercepted UNSUBSCRIBE of subscription id %s".formatted(subscriptionIds.get(0)));
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