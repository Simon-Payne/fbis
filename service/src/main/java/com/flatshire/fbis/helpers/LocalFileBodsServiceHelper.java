package com.flatshire.fbis.helpers;

import com.flatshire.fbis.FbisProperties;
import com.flatshire.fbis.components.BodsServiceHelper;
import com.flatshire.fbis.components.BusRouteReader;
import com.flatshire.fbis.csv.BusRouteBean;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.css.Counter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LocalFileBodsServiceHelper implements BodsServiceHelper {

    private static final Logger log = LoggerFactory.getLogger(LocalFileBodsServiceHelper.class);

    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<BusRouteBean>> routes = new ConcurrentHashMap<>();

    private final BusRouteReader busRouteReader;
    private final Map<String, String> properties;

    public LocalFileBodsServiceHelper(FbisProperties properties, BusRouteReader busRouteReader) {
        this.properties = FbisProperties.propertyMap(properties);
        this.busRouteReader = busRouteReader;
        Objects.requireNonNull(properties, "FbisProperties was null");
        Objects.requireNonNull(busRouteReader, "BusRouteReader was null");
        initialiseRoutes();
    }

    public void initialiseRoutes() {
        if(properties.get("lineRefs") != null) {
            Arrays.stream(properties.get("lineRefs").split(",")).forEach(this::initialiseRoute);
        }
    }

    @Override
    public Pair<String, String> fetchData(String lineRef) {
        Objects.requireNonNull(lineRef, "Line Ref was null");

        if (properties == null || properties.get("lineRefs") == null
                || !properties.get("lineRefs").contains(lineRef)) {
            throw new IllegalArgumentException("Line Ref %s is not configured in this service"
                    .formatted(lineRef));
        }

        if (routes.get(lineRef).isEmpty()) {
            throw new IllegalStateException("No bus route data found for route %s, check service configuration"
                    .formatted(lineRef));
        }

        Counter counter = counters.get(lineRef);
        if (counter == null) {
            throw new IllegalArgumentException("Invalid Line Ref '%s'".formatted(lineRef));
        }
        int nextCounter = counter.incrementWithReset(routes.get(lineRef).size());
        log.info("Line {} current counter {}", lineRef, nextCounter);
        BusRouteBean busRouteBean = routes.get(lineRef).get(nextCounter);

        return new ImmutablePair<>(busRouteBean.getLatitude().toPlainString(),
                busRouteBean.getLongitude().toPlainString());
    }

    private void initialiseRoute(String lineRef) {
        int line;
        try {
            line = Integer.parseInt(lineRef); // TODO make more flexible (letters or digits e.g. route 'X99')
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Line Ref '%s'".formatted(lineRef));
        }
        counters.put(lineRef, new Counter());
        String lineRouteFile = "route%d.csv".formatted(line);
        try {
            Path routeFileFolder = Path.of(properties.get("routeFileFolder"));
            Path routeFilePath = routeFileFolder.resolve(lineRouteFile);
            List<BusRouteBean> busRouteBeans = this.busRouteReader.readBusRouteFromCsvFile(routeFilePath);
            routes.put(lineRef, busRouteBeans);
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class Counter {
        int value;

        int incrementWithReset(int limit) {
            if (value >= limit) {
                value = 0;
            }
            return value++;
        }
    }
}
