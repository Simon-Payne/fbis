package com.flatshire.fbis.helpers;

import com.flatshire.fbis.DataFeedServiceException;
import com.flatshire.fbis.DataFeedServiceUnavailableException;
import com.flatshire.fbis.FbisProperties;
import com.flatshire.fbis.domain.BusInfo;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.org.siri.siri21.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.flatshire.fbis.FbisProperties.API_KEY;

public class DataFeedBodsServiceHelper implements BodsServiceHelper {

    private static final Logger log = LoggerFactory.getLogger(DataFeedBodsServiceHelper.class);

    public static final String TOKEN_PLACEHOLDER = "********";
    private final Map<String, String> properties;
    private final RestTemplate restTemplate;

    public DataFeedBodsServiceHelper(FbisProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = FbisProperties.propertyMap(properties);
        this.restTemplate = restTemplateBuilder.build();
        Objects.requireNonNull(properties, "FbisProperties was null");
        Objects.requireNonNull(restTemplate, "Rest Template was null");
    }

    @Override
    public Pair<String, String>  fetchData(String lineRef) throws DataFeedServiceUnavailableException {
        Objects.requireNonNull(lineRef, "Line Ref was null");
        if(properties.get(API_KEY) == null) {
            throw new IllegalStateException("API Key not supplied");
        }

        String urlTemplate = String.format("%s?operatorRef=%s&lineRef=%s&api_key=%s",
                properties.get("dataFeedUri"),
                properties.get("operatorRef"),
                lineRef,
                TOKEN_PLACEHOLDER);
        log.info(urlTemplate);

        Siri dataset;
        try {
            dataset = restTemplate.getForObject(urlTemplate
                    .replace(TOKEN_PLACEHOLDER, properties.get(API_KEY)), Siri.class);
            if(dataset == null) {
                throw new IllegalStateException("Dataset was null");
            }
            return getCoordinatesFromDataset(dataset, lineRef);

        } catch (HttpClientErrorException e) {
            if(e.getStatusCode().is5xxServerError()) {
                throw new DataFeedServiceUnavailableException(e);
            } else {
                throw new DataFeedServiceException(e);
            }
        }
        catch (RestClientException e) {
            throw new DataFeedServiceUnavailableException(e);
        }

    }

    @Override
    public List<BusInfo> fetchBusInfo(String operatorRef) {
        return null;
    }

    private static Pair<String, String> getCoordinatesFromDataset(Siri dataset, String lineRef) {
        ServiceDelivery serviceDelivery = dataset.getServiceDelivery();
        List<VehicleMonitoringDeliveryStructure> abstractFunctionalServiceDelivery =
                serviceDelivery.getVehicleMonitoringDeliveries();
        return abstractFunctionalServiceDelivery.stream()
                .findAny().map(vehicleMonitoring -> findCoords((VehicleMonitoringDeliveryStructure) vehicleMonitoring, lineRef))
                .orElse(ImmutablePair.nullPair());
    }

    private static Pair<String, String> findCoords(VehicleMonitoringDeliveryStructure vehicleMonitoring, String lineRef) {
        List<VehicleActivityStructure> vehicleActivity = vehicleMonitoring.getVehicleActivities();
        if(vehicleActivity.isEmpty()) {
            log.debug("No vehicle activity found for lineRef {}", lineRef);
            return ImmutablePair.nullPair();
        } else {
            // get last recorded vehicle activity entry in vehicleMonitoring list
            Comparator<VehicleActivityStructure> inReverseRecordedTimeOrder = (va, vb) -> Math.toIntExact(vb.getRecordedAtTime().toEpochSecond() - va.getRecordedAtTime().toEpochSecond());
            Stream<VehicleActivityStructure> vehicleActivities = vehicleActivity.stream().sorted(inReverseRecordedTimeOrder);
            VehicleActivityStructure.MonitoredVehicleJourney monitoredVehicleJourney = vehicleActivities.toList().get(0).getMonitoredVehicleJourney();
            LocationStructure vehicleLocation = monitoredVehicleJourney.getVehicleLocation();
            return new ImmutablePair<>(vehicleLocation.getLatitude().toPlainString(),
                    vehicleLocation.getLongitude().toPlainString());
        }
    }
}
