package com.flatshire.fbis.helpers;

import com.flatshire.fbis.DataFeedServiceException;
import com.flatshire.fbis.FbisProperties;
import com.flatshire.fbis.DataFeedServiceUnavailableException;
import com.flatshire.fbis.components.BodsServiceHelper;
import jakarta.xml.bind.JAXBElement;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.org.siri.siri.*;

import java.util.List;
import java.util.Objects;

public class DataFeedBodsServiceHelper implements BodsServiceHelper {

    private static final Logger log = LoggerFactory.getLogger(DataFeedBodsServiceHelper.class);

    public static final String TOKEN_PLACEHOLDER = "********";
    private final FbisProperties properties;
    private final RestTemplate restTemplate;

    public DataFeedBodsServiceHelper(FbisProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        Objects.requireNonNull(properties, "FbisProperties was null");
        Objects.requireNonNull(restTemplate, "Rest Template was null");
    }

    @Override
    public Pair<String, String> fetchData(String lineRef) throws DataFeedServiceUnavailableException {
        Objects.requireNonNull(lineRef, "Line Ref was null");
        if(properties.getApiKey() == null) {
            throw new IllegalStateException("API Key not supplied");
        }

        String urlTemplate = "%s?operatorRef=%s&api_key=%s".formatted(
                properties.getDataFeedUri(),
                properties.getOperatorRef(),
                TOKEN_PLACEHOLDER);
        log.info(urlTemplate);

        Siri dataset;
        try {
            dataset = restTemplate.getForObject(urlTemplate
                    .replace(TOKEN_PLACEHOLDER, properties.getApiKey()), Siri.class);

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
        Objects.requireNonNull(dataset, "DataFeed was null");

        return getCoordinatesFromDataset(dataset);

    }

    private static Pair<String, String> getCoordinatesFromDataset(Siri dataset) {
        ServiceDelivery serviceDelivery = dataset.getServiceDelivery();
        List<JAXBElement<? extends AbstractServiceDeliveryStructure>> abstractFunctionalServiceDelivery =
                serviceDelivery.getAbstractFunctionalServiceDelivery();
        return abstractFunctionalServiceDelivery.stream()
                .filter(delivery -> delivery.getValue() instanceof VehicleMonitoringDeliveryStructure)
                .findAny().map(vehicleMonitoring -> findCoords((VehicleMonitoringDeliveryStructure) vehicleMonitoring.getValue()))
                .orElseThrow();
    }

    private static Pair<String, String> findCoords(VehicleMonitoringDeliveryStructure vehicleMonitoring) {
        List<VehicleActivityStructure> vehicleActivity = vehicleMonitoring.getVehicleActivity();
        if(vehicleActivity.isEmpty()) {
            throw new IllegalStateException("No vehicle activity found");
        } else {
            VehicleActivityStructure.MonitoredVehicleJourney monitoredVehicleJourney = vehicleActivity.get(0)
                    .getMonitoredVehicleJourney();
            LocationStructure vehicleLocation = monitoredVehicleJourney.getVehicleLocation();
            return new ImmutablePair<>(vehicleLocation.getLatitude().toPlainString(),
                    vehicleLocation.getLongitude().toPlainString());
        }
    }
}
