package com.flatshire.fbis.components;

import com.flatshire.fbis.FbisProperties;
import com.flatshire.fbis.helpers.DataFeedBodsServiceHelper;
import com.flatshire.fbis.helpers.LocalFileBodsServiceHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Service
public class BodsServiceImpl implements BodsService {

    private static final Logger log = LoggerFactory.getLogger(BodsServiceImpl.class);

    private final Map<String, String> properties;
    private final RestTemplate restTemplate;
    private final BusRouteReader busRouteReader;
    private final ServiceMode serviceMode;
    private final BodsServiceHelper serviceHelper;

    @Autowired
    public BodsServiceImpl(FbisProperties properties, RestTemplate restTemplate, BusRouteReader busRouteReader) {
        this.properties = FbisProperties.propertyMap(properties);;
        this.restTemplate = restTemplate;
        this.busRouteReader = busRouteReader;
        this.serviceMode = ServiceMode.fromProperties(properties.getServiceMode());
        this.serviceHelper = serviceMode == ServiceMode.DATAFEED ?
                new DataFeedBodsServiceHelper(properties, restTemplate)
                : new LocalFileBodsServiceHelper(properties, busRouteReader);
    }

    @Override
    public Pair<String, String> readPositionFromDataFeed(String lineRef) {
        Objects.requireNonNull(lineRef, "Line Ref must not be null");
        log.debug("Reading bus {} position data in {} service mode", lineRef, serviceMode.getModeDesc());
        return serviceHelper.fetchData(lineRef);
    }

}
