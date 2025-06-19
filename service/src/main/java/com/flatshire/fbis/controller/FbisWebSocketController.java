package com.flatshire.fbis.controller;

import com.flatshire.fbis.messages.BusPositionResponse;
import com.flatshire.fbis.service.BodsServiceImpl;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

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

    public Triple<LocalDateTime, String, String> readFeedForLineRef(String lineRef) {
        Triple<LocalDateTime, String, String> dataFeed = bodsService.readPositionFromDataFeed(lineRef);
        if(!dataFeed.equals(ImmutableTriple.nullTriple())) {
            log.debug("Datafeed bus {}: {},{} [at {}]",
                    lineRef,
                    dataFeed.getMiddle(),
                    dataFeed.getRight(),
                    dataFeed.getLeft());
        } else {
            log.trace("Datafeed bus {} is empty", lineRef);
        }
        return dataFeed;
    }

    public void pushUpdateToLineRef(String lineRef, Triple<LocalDateTime, String, String> coordinates) {
        BusPositionResponse busPositionResponse = new BusPositionResponse(lineRef,
                coordinates.getLeft(),
                coordinates.getMiddle(),
                coordinates.getRight());
        messagingTemplate.convertAndSend("/topic/buspos/%s/".formatted(lineRef), busPositionResponse);
        log.debug("Map update {}", busPositionResponse);
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
