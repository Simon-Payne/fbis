package com.flatshire.fbis.components;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ScheduledTasks {

    private final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final Map<String, Pair<String, String>> locationMap = new HashMap<>();

    private final ApplicationContext applicationContext;

    @Value("${line.refs}")
    private List<String> lineRefs;

    @Autowired
    public ScheduledTasks(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    @Scheduled(fixedRate = 10000)
    public void readDataFeeds() {
        lineRefs.forEach(this::fetchBusPosition);
    }

    @Scheduled(fixedRate = 10000, initialDelay = 5000)
    public void pushUpdates() {
        lineRefs.forEach(this::pushBusPosition);
    }

    private void pushBusPosition(String lineRef) {
        if (locationMap.containsKey(lineRef)) {
            Pair<String, String> coordinates = locationMap.get(lineRef);
            FbisService fbisService = applicationContext.getBean(FbisService.class);
            fbisService.pushUpdateToLineRef(lineRef, coordinates);
        } else {
            log.error("Bus {} has no stored locations", lineRef);
        }
    }

    private void fetchBusPosition(String lineRef) {
        FbisService fbisService = applicationContext.getBean(FbisService.class);
        locationMap.put(lineRef, fbisService.readFeedForLineRef(lineRef));
    }
}
