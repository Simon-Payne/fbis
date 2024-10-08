package com.flatshire.fbis.components;

import com.flatshire.fbis.messages.BusPositionResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class FbisWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(FbisWebSocketController.class);

    private final BodsServiceImpl bodsService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public FbisWebSocketController(BodsServiceImpl bodsService, SimpMessagingTemplate messagingTemplate) {
        this.bodsService = bodsService;
        this.messagingTemplate = copyMessagingTemplate(messagingTemplate);
    }

    public Pair<String, String> readFeedForLineRef(String lineRef) {
        Pair<String, String> dataFeed = bodsService.readPositionFromDataFeed(lineRef);
        log.info("Datafeed bus {} - {}:{}",
                lineRef,
                dataFeed.getLeft(),
                dataFeed.getRight());
        return dataFeed;
    }

    public void pushUpdateToLineRef(String lineRef, Pair<String, String> coordinates) {
        BusPositionResponse busPositionResponse = new BusPositionResponse(lineRef,
                coordinates.getLeft(),
                coordinates.getRight());
        messagingTemplate.convertAndSend("/topic/buspos/%s/".formatted(lineRef), busPositionResponse);
        log.info("Map update {}", busPositionResponse);
    }

    private static SimpMessagingTemplate copyMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        SimpMessagingTemplate copy = new SimpMessagingTemplate(messagingTemplate.getMessageChannel());
        copy.setHeaderInitializer(messagingTemplate.getHeaderInitializer());
        copy.setSendTimeout(messagingTemplate.getSendTimeout());
        copy.setUserDestinationPrefix(messagingTemplate.getUserDestinationPrefix());
        copy.setDefaultDestination(messagingTemplate.getDefaultDestination());
        copy.setMessageConverter(messagingTemplate.getMessageConverter());
        return copy;
    }
}
