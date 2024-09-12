package com.flatshire.fbis.components;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.support.ChannelInterceptor;

public class BusServiceChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        String payload = (String) message.getPayload();
        if(payload.equals("125")) {
            return message;
        } else {
            throw new MessageDeliveryException("Unknown bus number " + payload);
        }
    }
}
