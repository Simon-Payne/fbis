package com.flatshire.fbis.components;

import com.flatshire.fbis.DataFeedServiceException;
import com.flatshire.fbis.DataFeedServiceUnavailableException;
import com.flatshire.fbis.FbisProperties;
import com.flatshire.fbis.helpers.DataFeedBodsServiceHelper;
import jakarta.xml.bind.JAXBElement;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.org.siri.siri21.*;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataFeedBodsServiceHelperTest {

    @Mock
    private FbisProperties properties;
    @Mock
    private RestTemplateBuilder restTemplateBuilder;
    @Mock
    private Siri dataset;

    @Test
    void shouldHandleNullsInConstructor() {
        assertThrows(NullPointerException.class, () -> new DataFeedBodsServiceHelper(null, null));
        assertThrows(NullPointerException.class, () -> new DataFeedBodsServiceHelper(properties, null));
    }

    @Test
    void shouldRejectNullLineRef() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        NullPointerException exception = assertThrows(NullPointerException.class, () -> new DataFeedBodsServiceHelper(properties,
                restTemplateBuilder).fetchData(null));
        assertThat(exception.getMessage(), equalTo("Line Ref was null"));
    }

    @Test
    void shouldHandleApiKeyNotConfigured() {
        when(properties.getApiKey()).thenReturn(null); // just being explicit
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        DataFeedBodsServiceHelper objectUnderTest = new DataFeedBodsServiceHelper(properties, restTemplateBuilder);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> objectUnderTest.fetchData("1"));
        assertThat(exception.getMessage(), equalTo("API Key not supplied"));
    }

    @Test
    void shouldHandleRest404() {
        when(properties.getDataFeedUri()).thenReturn("data feed uri");
        when(properties.getOperatorRef()).thenReturn("operator ref");
        when(properties.getApiKey()).thenReturn("api key");
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(anyString(), eq(Siri.class)))
                .thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404)));
        DataFeedBodsServiceHelper objectUnderTest = new DataFeedBodsServiceHelper(properties, restTemplateBuilder);
        DataFeedServiceException exception = assertThrows(DataFeedServiceException.class,
                () -> objectUnderTest.fetchData("some line ref"));
        assertThat(exception.getMessage(), equalTo("Service error, cause was \"404 NOT_FOUND\""));
    }

    @Test
    void shouldHandleWhenDataFeedThrowsArbitraryException() {
        when(properties.getDataFeedUri()).thenReturn("data feed uri");
        when(properties.getOperatorRef()).thenReturn("operator ref");
        when(properties.getApiKey()).thenReturn("api key");
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(anyString(), eq(Siri.class)))
                .thenThrow(new RestClientException("Data Feed is down"));
        DataFeedBodsServiceHelper objectUnderTest = new DataFeedBodsServiceHelper(properties, restTemplateBuilder);
        DataFeedServiceUnavailableException exception = assertThrows(DataFeedServiceUnavailableException.class,
                () -> objectUnderTest.fetchData("1"));
        assertThat(exception.getMessage(),
                equalTo("Service unavailable, advice was \"Data Feed is down\""));
    }

    @Test
    void shouldServiceValidRouteRequest() {
        when(properties.getDataFeedUri()).thenReturn("data feed uri");
        when(properties.getOperatorRef()).thenReturn("operator ref");
        when(properties.getApiKey()).thenReturn("api key");
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(anyString(), eq(Siri.class))).thenReturn(dataset);
        configureMockDataset();
        DataFeedBodsServiceHelper objectUnderTest = new DataFeedBodsServiceHelper(properties, restTemplateBuilder);
        Pair<String, String> feedResponse = objectUnderTest.fetchData("1");
        assertThat(feedResponse, equalTo(new ImmutablePair<>("10", "10")));
    }

    private void configureMockDataset() {
        ServiceDelivery mockServiceDelivery = mock(ServiceDelivery.class);
        when(dataset.getServiceDelivery()).thenReturn(mockServiceDelivery);
        VehicleMonitoringDeliveryStructure vehicleMonitoringDeliveryStructure =
                mock(VehicleMonitoringDeliveryStructure.class);
        VehicleActivityStructure vehicleActivityStructure = mock(VehicleActivityStructure.class);
        when(vehicleMonitoringDeliveryStructure.getVehicleActivities()).thenReturn(List.of(vehicleActivityStructure));
        VehicleActivityStructure.MonitoredVehicleJourney monitoredVehicleJourney =
                mock(VehicleActivityStructure.MonitoredVehicleJourney.class);
        when(vehicleActivityStructure.getMonitoredVehicleJourney()).thenReturn(monitoredVehicleJourney);
        LocationStructure vehicleLocation = mock(LocationStructure.class);
        when(vehicleLocation.getLatitude()).thenReturn(BigDecimal.TEN);
        when(vehicleLocation.getLongitude()).thenReturn(BigDecimal.TEN);
        when(monitoredVehicleJourney.getVehicleLocation()).thenReturn(vehicleLocation);
        when(mockServiceDelivery.getVehicleMonitoringDeliveries()).thenReturn(List.of(vehicleMonitoringDeliveryStructure));
    }

}
