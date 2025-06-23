package com.flatshire.fbis.helpers;

import com.flatshire.fbis.DataFeedServiceException;
import com.flatshire.fbis.DataFeedServiceUnavailableException;
import com.flatshire.fbis.FbisProperties;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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

    private static final Function<Integer, ZonedDateTime> fGetZonedDateTime = (i) -> ZonedDateTime.of(2000, 1, 1, 1, 1, 1, i, ZoneId.of("UTC"));

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
    void shouldHandleRest500() {
        when(properties.getDataFeedUri()).thenReturn("data feed uri");
        when(properties.getOperatorRef()).thenReturn("operator ref");
        when(properties.getApiKey()).thenReturn("api key");
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(anyString(), eq(Siri.class)))
                .thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(500)));
        DataFeedBodsServiceHelper objectUnderTest = new DataFeedBodsServiceHelper(properties, restTemplateBuilder);
        DataFeedServiceUnavailableException exception = assertThrows(DataFeedServiceUnavailableException.class,
                () -> objectUnderTest.fetchData("some line ref"));
        assertThat(exception.getMessage(), equalTo("Service unavailable, advice was \"500 INTERNAL_SERVER_ERROR\""));
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
    void shouldHandleCaseWhenDataFeedThrowsArbitraryException() {
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
    void shouldHandleCaseWhenFeedReturnsNullDataset() {
        when(properties.getDataFeedUri()).thenReturn("data feed uri");
        when(properties.getOperatorRef()).thenReturn("operator ref");
        when(properties.getApiKey()).thenReturn("api key");
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(anyString(), eq(Siri.class))).thenReturn(null);
        DataFeedBodsServiceHelper objectUnderTest = new DataFeedBodsServiceHelper(properties, restTemplateBuilder);
        DataFeedServiceException exception = assertThrows(DataFeedServiceException.class, () -> objectUnderTest.fetchData("1"));
        assertThat(exception.getMessage(), equalTo("Service error, cause was \"Dataset returned from feed was null\""));
    }

    @Test
    void shouldHandleValidRouteRequestWhenNoVehicleActivitiesAreReturned() {
        when(properties.getDataFeedUri()).thenReturn("data feed uri");
        when(properties.getOperatorRef()).thenReturn("operator ref");
        when(properties.getApiKey()).thenReturn("api key");
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(anyString(), eq(Siri.class))).thenReturn(dataset);
        configureMockDataset(0);
        DataFeedBodsServiceHelper objectUnderTest = new DataFeedBodsServiceHelper(properties, restTemplateBuilder);
        Triple<LocalDateTime, String, String> feedResponse = objectUnderTest.fetchData("1");
        assertThat(feedResponse, equalTo(ImmutableTriple.nullTriple()));
    }

    @Test
    void shouldServiceValidRouteRequest() {
        when(properties.getDataFeedUri()).thenReturn("data feed uri");
        when(properties.getOperatorRef()).thenReturn("operator ref");
        when(properties.getApiKey()).thenReturn("api key");
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(anyString(), eq(Siri.class))).thenReturn(dataset);
        configureMockDataset(1);
        DataFeedBodsServiceHelper objectUnderTest = new DataFeedBodsServiceHelper(properties, restTemplateBuilder);
        Triple<LocalDateTime, String, String> feedResponse = objectUnderTest.fetchData("1");
        LocalDateTime localDateTime = fGetZonedDateTime.apply(0).toLocalDateTime();
        assertThat(feedResponse, equalTo(new ImmutableTriple<>(localDateTime, "10", "10")));
    }

    @Test
    void shouldServiceRouteRequestWithTwoVehicleActivities() {
        when(properties.getDataFeedUri()).thenReturn("data feed uri");
        when(properties.getOperatorRef()).thenReturn("operator ref");
        when(properties.getApiKey()).thenReturn("api key");
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(anyString(), eq(Siri.class))).thenReturn(dataset);
        configureMockDataset(2);
        DataFeedBodsServiceHelper objectUnderTest = new DataFeedBodsServiceHelper(properties, restTemplateBuilder);
        Triple<LocalDateTime, String, String> feedResponse = objectUnderTest.fetchData("1");
        LocalDateTime localDateTime = fGetZonedDateTime.apply(1).toLocalDateTime();
        assertThat(feedResponse, equalTo(new ImmutableTriple<>(localDateTime, "10", "11")));
    }

    private void configureMockDataset(int numVehicleActivitiesRequired) {
        ServiceDelivery mockServiceDelivery = mock(ServiceDelivery.class);
        when(dataset.getServiceDelivery()).thenReturn(mockServiceDelivery);
        VehicleMonitoringDeliveryStructure vehicleMonitoringDeliveryStructure =
                mock(VehicleMonitoringDeliveryStructure.class);
        when(mockServiceDelivery.getVehicleMonitoringDeliveries()).thenReturn(List.of(vehicleMonitoringDeliveryStructure));
        List<VehicleActivityStructure> vehicleActivityStructures = mockVehicleActivities(numVehicleActivitiesRequired);
        when(vehicleMonitoringDeliveryStructure.getVehicleActivities())
                .thenReturn(vehicleActivityStructures);
    }

    private List<VehicleActivityStructure> mockVehicleActivities(int numRequired) {
        if (numRequired == 1) {
            VehicleActivityStructure vehicleActivityStructure = mockVehicleActivity(0);
            when(vehicleActivityStructure.getRecordedAtTime()).thenReturn(fGetZonedDateTime.apply(0));
            return List.of(vehicleActivityStructure);
        } else {
            List<VehicleActivityStructure> listOfMocks = new ArrayList<>();
            int i = 0;
            while (i++ < numRequired) {
                VehicleActivityStructure vehicleActivityStructure = mockVehicleActivity(i);
                when(vehicleActivityStructure.getRecordedAtTime()).thenReturn(fGetZonedDateTime.apply(i));
                listOfMocks.add(vehicleActivityStructure);
            }
            return listOfMocks;
        }
    }

    private static VehicleActivityStructure mockVehicleActivity(int index) {
        VehicleActivityStructure vehicleActivityStructure = mock(VehicleActivityStructure.class);
        VehicleActivityStructure.MonitoredVehicleJourney monitoredVehicleJourney =
                mock(VehicleActivityStructure.MonitoredVehicleJourney.class);
        lenient().when(vehicleActivityStructure.getMonitoredVehicleJourney()).thenReturn(monitoredVehicleJourney);
        LocationStructure vehicleLocation = mock(LocationStructure.class);
        lenient().when(vehicleLocation.getLatitude()).thenReturn(BigDecimal.TEN);
        // return differentiated longitude values to enable test to assert correct behaviour
        lenient().when(vehicleLocation.getLongitude()).thenReturn(new BigDecimal(10 + index));
        lenient().when(monitoredVehicleJourney.getVehicleLocation()).thenReturn(vehicleLocation);
        return vehicleActivityStructure;
    }

}
