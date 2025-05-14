package com.flatshire.fbis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FbisProperties {

    @Value( "${service.mode}")
    private String serviceMode;

    @Value( "${bods.dataset.uri}" )
    private String datasetUri;

    @Value( "${bods.datafeed.uri}" )
    private String dataFeedUri;

    @Value( "${bods.api.key}" )
    private String apiKey;

    @Value( "${dataset.id}")
    private String datasetId;

    @Value( "${operator.ref}")
    private String operatorRef;

    @Value( "${line.refs}")
    private String lineRefs;

    @Value( "${route.file.folder}")
    private String routeFileFolder;

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
}
