package com.flatshire.fbis.components;

import com.flatshire.fbis.csv.BusRouteBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusRouteReaderTest {

    private BusRouteReader objectUnderTest;

    @BeforeEach
    public void setUp() {
        objectUnderTest = new BusRouteReader();
    }

    @Test
    void shouldHandleNulls() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> objectUnderTest.readBusRouteFromCsvFile(null));
        assertThat(exception.getMessage(), equalTo("Path was null"));
    }

    @Test
    void shouldHandleNonExistentFile() {
        String nonExistent = "/non-existent";
        FileNotFoundException exception = assertThrows(FileNotFoundException.class,
                () -> objectUnderTest.readBusRouteFromCsvFile(Path.of(nonExistent)));
        assertThat(exception.getMessage(), equalTo("Non-existent file " + nonExistent));
    }

    @Test
    void shouldHandleUnprocessableFile() {
        Path unprocessable = getPathFromResource("/unprocessable.csv");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> objectUnderTest.readBusRouteFromCsvFile(unprocessable));
        assertThat(exception.getMessage(), equalTo("Invalid path 'unprocessable.csv': " +
                "did not start with 'route', continue with an integer and end with '.csv'"));
    }

    @Test
    void shouldHandleInvalidFile() throws IOException {
        Path p = getPathFromResource("/routes/route0something.csv");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> objectUnderTest.readBusRouteFromCsvFile(p));
        assertThat(exception.getMessage(), equalTo("Invalid path 'route0something.csv': " +
                "did not start with 'route', continue with an integer and end with '.csv'"));
    }

    @Test
    void shouldParseValidButEmptyFile() throws IOException, URISyntaxException {
        Path p = getPathFromResource("/routes/route0.csv");
        assertThat(objectUnderTest.readBusRouteFromCsvFile(p), hasSize(0));
    }

    @Test
    void shouldParseValidFileWithContents() throws IOException, URISyntaxException {
        Path p = getPathFromResource("/routes/route1.csv");
        List<BusRouteBean> busRouteBeans = objectUnderTest.readBusRouteFromCsvFile(p);
        assertThat(busRouteBeans, notNullValue());
        assertThat(busRouteBeans, hasSize(2));
        checkBusRouteBean(busRouteBeans.get(0), "description 1", "123", "456");
        checkBusRouteBean(busRouteBeans.get(1), "description 2", "789", "012");
    }

    private static void checkBusRouteBean(BusRouteBean busRouteBean1, String description, String latitude, String longitude) {
        assertThat(busRouteBean1.getDescription(), equalTo(description));
        assertThat(busRouteBean1.getLatitude(), equalTo(BigDecimal.valueOf(Double.parseDouble(latitude))));
        assertThat(busRouteBean1.getLongitude(), equalTo(BigDecimal.valueOf(Double.parseDouble(longitude))));
    }

    private Path getPathFromResource(String s) {
        return Path.of(Objects.requireNonNull(this.getClass().getResource(s)).getPath());
    }

}
