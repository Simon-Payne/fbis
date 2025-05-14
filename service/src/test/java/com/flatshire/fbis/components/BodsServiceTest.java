package com.flatshire.fbis.components;

import com.flatshire.fbis.FbisProperties;
import com.flatshire.fbis.domain.BusInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import uk.org.siri.siri21.Siri;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsEmptyCollection.emptyCollectionOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
@Import({BodsServiceImpl.class, FbisProperties.class, BusRouteReader.class})
@TestPropertySource(properties = "app.scheduling.enable=false")
@ActiveProfiles("test")
class BodsServiceTest {

    @MockBean
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private BodsServiceImpl objectUnderTest;

    @Test
    void shouldHandleNullLineRef() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> objectUnderTest.readPositionFromDataFeed(null));
        assertThat(exception.getMessage(), equalTo("Line Ref must not be null"));
    }

    @Test
    void shouldHandleInvalidLineRef() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplate.getForObject(anyString(), eq(Siri.class))).thenReturn(new Siri());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> objectUnderTest.readPositionFromDataFeed("invalid"));
        assertThat(exception.getMessage(), equalTo("Line Ref invalid is not configured in this service"));
    }

    @Test
    void shouldSupplyDataFeedForValidLineRef() {
        // test each position to beyond wrap-around
        checkPositionCoords("2", "52.391264", "0.263717");
        checkPositionCoords("2", "52.392935", "0.265813");
        checkPositionCoords("2", "52.397363", "0.259994");
        checkPositionCoords("2", "52.391264", "0.263717"); // wraparound
        checkPositionCoords("2", "52.392935", "0.265813");
        // test different line ref is unaffected
        checkPositionCoords("4", "123.0", "456.0");
    }

    @Test
    void shouldHandleNullOperatorRef() {
        OperatorNotSuppliedException exception = assertThrows(OperatorNotSuppliedException.class, () -> objectUnderTest.readBusInfo(null));
        assertThat(exception.getMessage(), equalTo("Operator reference cannot be null"));
    }

    @Test
    void shouldReturnWhereNoBusesFoundForOperator() {
        assertThat(objectUnderTest.readBusInfo("nonexistent"), emptyCollectionOf(BusInfo.class));
    }

    @Test
    void shouldReturnWhereBusesFoundForOperator() {
        List<BusInfo> busInfos = objectUnderTest.readBusInfo("LTEA");
        assertThat(busInfos, contains(
                BusInfo.of("LTEA", "117"),
                BusInfo.of("LTEA", "125"),
                BusInfo.of("LTEA", "129")));
    }

    private void checkPositionCoords(String lineRef, String latitude, String longitude) {
        Pair<String, String> position1 = objectUnderTest.readPositionFromDataFeed(lineRef);
        assertThat(position1, notNullValue());
        assertThat(position1.getLeft(), equalTo(latitude));
        assertThat(position1.getRight(), equalTo(longitude));
    }

}
