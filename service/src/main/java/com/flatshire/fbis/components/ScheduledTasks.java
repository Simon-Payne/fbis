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
        updateLineDataFromFeed(applicationContext.getBean(FbisService.class), "117");
        updateLineDataFromFeed(applicationContext.getBean(FbisService.class), "125");
        updateLineDataFromFeed(applicationContext.getBean(FbisService.class), "129");
    }

    private void updateLineDataFromFeed(FbisService fbisService, String lineRef) {
        locationMap.put(lineRef, fbisService.readFeedForLineRef(lineRef));
    }

    @Scheduled(fixedRate = 10000, initialDelay = 5000)
    public void pushUpdates() {
        pushUpdateToLineRef(applicationContext.getBean(FbisService.class), "117");
        pushUpdateToLineRef(applicationContext.getBean(FbisService.class), "125");
        pushUpdateToLineRef(applicationContext.getBean(FbisService.class), "129");
    }

    private void pushUpdateToLineRef(FbisService fbisService, String lineRef) {
        if (locationMap.containsKey(lineRef)) {
            Pair<String, String> coordinates = locationMap.get(lineRef);
            fbisService.pushUpdateToLineRef(lineRef, coordinates);
        } else {
            log.error("Bus {} has no stored locations", lineRef);
        }
    }
}
