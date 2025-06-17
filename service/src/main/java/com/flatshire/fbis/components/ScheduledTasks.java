package com.flatshire.fbis.components;

import com.flatshire.fbis.FbisProperties;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ScheduledTasks {

    private final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final Map<String, Pair<String, String>> locationMap;

    private final ApplicationContext applicationContext;

    private final Map<String, String> properties;

    @Autowired
    public ScheduledTasks(FbisProperties properties, ApplicationContext applicationContext, Map<String, Pair<String, String>> locationMap) {
        this.applicationContext = applicationContext;
        this.properties = FbisProperties.propertyMap(properties);
        this.locationMap = new HashMap<>(locationMap);
    }

    @Scheduled(fixedRateString = "${push.notification.delay}")
    public void readDataFeeds() {
        List<String> lineRefs = splitLineRefs(properties.get("lineRefs"));
        lineRefs.forEach(this::fetchBusPosition);
    }

    @Scheduled(fixedRateString = "${push.notification.delay}", initialDelayString = "${push.notification.delay.initial}")
    public void pushUpdates() {
        List<String> lineRefs = splitLineRefs(properties.get("lineRefs"));
        lineRefs.forEach(this::pushBusPosition);
    }

    Map<String, Pair<String, String>> reportMapContents() {
        return new HashMap<>(locationMap);
    }

    private void pushBusPosition(String lineRef) {
        if (locationMap.containsKey(lineRef)) {
            Pair<String, String> coordinates = locationMap.get(lineRef);
            FbisWebSocketController fbisWebSocketController = applicationContext.getBean(FbisWebSocketController.class);
            fbisWebSocketController.pushUpdateToLineRef(lineRef, coordinates);
        } else {
            log.error("Bus {} has no stored locations", lineRef);
        }
    }

    private void fetchBusPosition(String lineRef) {
        FbisWebSocketController fbisWebSocketController = applicationContext.getBean(FbisWebSocketController.class);
        Pair<String, String> positionPair = fbisWebSocketController.readFeedForLineRef(lineRef);
        if(!positionPair.equals(ImmutablePair.nullPair())) {
            locationMap.put(lineRef, positionPair);
        }
    }

    private List<String> splitLineRefs(String lineRefs) {
        if (lineRefs == null || lineRefs.isEmpty()) {
            return Optional.of(new ArrayList<String>()).orElseThrow(IllegalArgumentException::new);
        }

        return Arrays.stream(lineRefs.split(","))
                .filter(lineRef -> !lineRef.trim().isEmpty())
                .collect(Collectors.toList());
    }
}
