package com.flatshire.fbis.components;

import com.flatshire.fbis.FbisProperties;
import com.flatshire.fbis.csv.BusRouteBean;
import com.flatshire.fbis.helpers.LocalFileBodsServiceHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalFileBodsServiceHelperTest {

    @Mock
    private BusRouteReader busRouteReader;

    @Mock
    private FbisProperties properties;

    @Test
    void shouldHandleNullsInConstructor() {
        assertThrows(NullPointerException.class, () -> new LocalFileBodsServiceHelper(null, null));
        assertThrows(NullPointerException.class, () -> new LocalFileBodsServiceHelper(properties, null));
    }

    @Test
    void shouldRejectNullLineRef() {
        LocalFileBodsServiceHelper objectUnderTest = new LocalFileBodsServiceHelper(properties, busRouteReader);
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> objectUnderTest.fetchData(null));
        assertThat(exception.getMessage(), equalTo("Line Ref was null"));
    }

    @Test
    void shouldRejectUnconfiguredLineRef() {
        LocalFileBodsServiceHelper objectUnderTest = new LocalFileBodsServiceHelper(properties, busRouteReader);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> objectUnderTest.fetchData("abc"));
        assertThat(exception.getMessage(), equalTo("Line Ref abc is not configured in this service"));
    }

    @Test
    void shouldRejectValidRouteWithMissingRouteFile() throws IOException, URISyntaxException {
        when(properties.getLineRefs()).thenReturn("3");
        when(properties.getRouteFileFolder()).thenReturn("routes");
        when(busRouteReader.readBusRouteFromCsvFile(any(Path.class)))
                .thenThrow(new FileNotFoundException("something went wrong with route 3"));
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () ->  new LocalFileBodsServiceHelper(properties, busRouteReader));
        assertThat(exception.getCause().getMessage(),
                equalTo("something went wrong with route 3"));
    }

    @Test
    void shouldRejectValidRouteLackingBusData() throws IOException, URISyntaxException {
        when(properties.getLineRefs()).thenReturn("0");
        when(properties.getRouteFileFolder()).thenReturn("routes");
        when(busRouteReader.readBusRouteFromCsvFile(any(Path.class))).thenReturn(List.of());
        LocalFileBodsServiceHelper objectUnderTest = new LocalFileBodsServiceHelper(properties, busRouteReader);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> objectUnderTest.fetchData("0"));
        assertThat(exception.getMessage(),
                equalTo("No bus route data found for route 0, check service configuration"));
    }

    @Test
    void shouldServiceValidRouteRequest() throws IOException, URISyntaxException {
        when(properties.getLineRefs()).thenReturn("1");
        when(properties.getRouteFileFolder()).thenReturn("routes");
        when(busRouteReader.readBusRouteFromCsvFile(any(Path.class))).thenReturn(List.of(
                new BusRouteBean().setDescription("desc 1")
                        .setLatitude(BigDecimal.valueOf(0.0))
                        .setLongitude(BigDecimal.valueOf(1.0)),
                new BusRouteBean().setDescription("desc 2")
                        .setLatitude(BigDecimal.valueOf(2.0))
                        .setLongitude(BigDecimal.valueOf(3.0))));
        LocalFileBodsServiceHelper objectUnderTest = new LocalFileBodsServiceHelper(properties, busRouteReader);
        Pair<String, String> data = objectUnderTest.fetchData("1");
        assertThat(data, notNullValue());
        // TODO add assertion for description once supplied
        assertThat(data.getLeft(), equalTo("0.0"));
        assertThat(data.getRight(), equalTo("1.0"));
    }

}
