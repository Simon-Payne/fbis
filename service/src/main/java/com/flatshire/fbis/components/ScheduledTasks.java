package com.flatshire.fbis.components;

import com.flatshire.fbis.messages.BusPositionResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    static final Map<String, Pair<String, String>> locationMap = new HashMap<>();
    private final List<String> lineRefs = List.of("117", "125", "129");

    private final BodsServiceImpl bodsService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ScheduledTasks(BodsServiceImpl bodsService, SimpMessagingTemplate messagingTemplate) {
        this.bodsService = bodsService;
        this.messagingTemplate = copyMessagingTemplate(messagingTemplate);
    }

    private SimpMessagingTemplate copyMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        SimpMessagingTemplate copy = new SimpMessagingTemplate(messagingTemplate.getMessageChannel());
        copy.setHeaderInitializer(messagingTemplate.getHeaderInitializer());
        copy.setSendTimeout(messagingTemplate.getSendTimeout());
        copy.setUserDestinationPrefix(messagingTemplate.getUserDestinationPrefix());
        copy.setDefaultDestination(messagingTemplate.getDefaultDestination());
        copy.setMessageConverter(messagingTemplate.getMessageConverter());
        return copy;
    }

    @Scheduled(fixedRate = 10000)
    public void readDataFeeds() {
        lineRefs.forEach(this::readFeedForLineRef);
    }

    @Scheduled(fixedRate = 10000, initialDelay = 5000)
    public void pushUpdates() {
        lineRefs.forEach(this::pushUpdateForLineRef);
    }

    private void readFeedForLineRef(String lineRef) {
        Pair<String, String> dataFeed = bodsService.readPositionFromDataFeed(lineRef);
        log.info("Datafeed bus {} - {}:{}",
                lineRef,
                dataFeed.getLeft(),
                dataFeed.getRight());
        locationMap.put(lineRef, dataFeed);
    }

    private void pushUpdateForLineRef(String lineRef) {
        if(locationMap.containsKey(lineRef)) {
            Pair<String, String> coordinates = locationMap.get(lineRef);
            log.info("Map update bus {} - {}:{}",
                    lineRef,
                    coordinates.getLeft(),
                    coordinates.getRight());
            messagingTemplate.convertAndSend("/topic/buspos/%s/".formatted(lineRef),
                    new BusPositionResponse(lineRef,
                            coordinates.getLeft(),
                            coordinates.getRight()));
        } else {
            log.error("Bus {} not found", lineRef);
        }
    }

}
