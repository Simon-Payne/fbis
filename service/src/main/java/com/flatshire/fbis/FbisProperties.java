package com.flatshire.fbis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class FbisProperties {

    private static final String SERVICE_MODE = "serviceMode";
    private static final String DATASET_URI = "datasetUri";
    private static final String DATA_FEED_URI = "dataFeedUri";
    public static final String API_KEY = "apiKey";
    private static final String DATASET_ID = "datasetId";
    private static final String OPERATOR_REF = "operatorRef";
    private static final String LINE_REFS = "lineRefs";
    private static final String ROUTE_FILE_FOLDER = "routeFileFolder";
    private static final String PUSH_NOTIFICATION_DELAY = "pushNotificationDelay";

    @Value("${service.mode}")
    private String serviceMode;

    @Value("${bods.dataset.uri}")
    private String datasetUri;

    @Value("${bods.datafeed.uri}")
    private String dataFeedUri;

    @Value("${bods.api.key}")
    private String apiKey;

    @Value("${dataset.id}")
    private String datasetId;

    @Value("${operator.ref}")
    private String operatorRef;

    @Value("${line.refs}")
    private String lineRefs;

    @Value("${route.file.folder}")
    private String routeFileFolder;

    @Value( "${push.notification.delay}")
    private Long pushNotificationDelay;

    public String getServiceMode() {
        return serviceMode;
    }

    public void setServiceMode(String serviceMode) {
        this.serviceMode = serviceMode;
    }

    public String getDatasetUri() {
        return datasetUri;
    }

    public String getDataFeedUri() {
        return dataFeedUri;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public String getOperatorRef() {
        return operatorRef;
    }

    public String getLineRefs() {
        return lineRefs;
    }

    public String getRouteFileFolder() {
        return routeFileFolder;
    }

    public Long getPushNotificationDelay() {
        return pushNotificationDelay;
    }

    public static Map<String, String> propertyMap(FbisProperties fbisProperties) {
        if (fbisProperties == null) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        if (fbisProperties.getServiceMode() != null) {
            map.put(SERVICE_MODE, fbisProperties.getServiceMode());
        }
        if (fbisProperties.getDatasetUri() != null) {
            map.put(DATASET_URI, fbisProperties.getDatasetUri());
        }
        if (fbisProperties.getDataFeedUri() != null) {
            map.put(DATA_FEED_URI, fbisProperties.getDataFeedUri());
        }
        if (fbisProperties.getApiKey() != null) {
            map.put(API_KEY, fbisProperties.getApiKey());
        }
        if (fbisProperties.getDatasetId() != null) {
            map.put(DATASET_ID, fbisProperties.getDatasetId());
        }
        if (fbisProperties.getOperatorRef() != null) {
            map.put(OPERATOR_REF, fbisProperties.getOperatorRef());
        }
        if (fbisProperties.getLineRefs() != null) {
            map.put(LINE_REFS, fbisProperties.getLineRefs());
        }
        if (fbisProperties.getRouteFileFolder() != null) {
            map.put(ROUTE_FILE_FOLDER, fbisProperties.getRouteFileFolder());
        }
        if (fbisProperties.getPushNotificationDelay() != null) {
            map.put(PUSH_NOTIFICATION_DELAY, String.valueOf(fbisProperties.getPushNotificationDelay()));
        }
        return map;
    }
}
