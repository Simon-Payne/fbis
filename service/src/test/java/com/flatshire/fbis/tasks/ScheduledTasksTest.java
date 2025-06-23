package com.flatshire.fbis.tasks;

import com.flatshire.fbis.FbisProperties;
import com.flatshire.fbis.controller.FbisWebSocketController;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledTasksTest {

    @Mock
    private FbisProperties properties;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private FbisWebSocketController webSocketController;

    private ScheduledTasks objectUnderTest;

    private static final Triple<LocalDateTime, String, String> pair123 = ImmutableTriple.of(LocalDateTime.of(LocalDate.of(2025, 6, 1), LocalTime.of(10, 30, 0, 0)), "1234567890.1", "2345678901.2");
    private static final Triple<LocalDateTime, String, String> pair456 = ImmutableTriple.of(LocalDateTime.of(LocalDate.of(2025, 6, 1), LocalTime.of(10, 35, 0, 0)), "4234567890.1", "4345678901.2");

    @Test
    void givenEmptyLineRefsShouldNotStoreAnyData() {
        when(properties.getLineRefs()).thenReturn("");
        objectUnderTest = new ScheduledTasks(properties, applicationContext, Map.of());
        objectUnderTest.readDataFeeds();
        verify(webSocketController, never()).readFeedForLineRef(anyString());
        assertThat(objectUnderTest.reportMapContents().isEmpty(), is(true));
    }

    @Test
    void givenLineRefHavingNoFeedResultsShouldNotStoreAnyData() {
        when(properties.getLineRefs()).thenReturn("789");
        when(applicationContext.getBean(FbisWebSocketController.class)).thenReturn(webSocketController);
        when(webSocketController.readFeedForLineRef(anyString())).thenReturn(ImmutableTriple.nullTriple());
        objectUnderTest = new ScheduledTasks(properties, applicationContext, Map.of());
        objectUnderTest.readDataFeeds();
        verify(webSocketController).readFeedForLineRef("789");
        assertThat(objectUnderTest.reportMapContents().isEmpty(), is(true));
    }

    @Test
    void givenMultipleLineRefsHavingFeedResultsShouldStoreDataForEach() {
        when(properties.getLineRefs()).thenReturn("123,456");
        when(applicationContext.getBean(FbisWebSocketController.class)).thenReturn(webSocketController);
        when(webSocketController.readFeedForLineRef(anyString())).thenReturn(pair123).thenReturn(pair456);
        objectUnderTest = new ScheduledTasks(properties, applicationContext, Map.of());
        objectUnderTest.readDataFeeds();
        verify(webSocketController).readFeedForLineRef("123");
        verify(webSocketController).readFeedForLineRef("456");
        assertThat(objectUnderTest.reportMapContents().keySet().containsAll(List.of("123", "456")), is((true)));
    }

    @Test
    void givenLineRefHavingNoStoredDataShouldNotPushAnyUpdates() {
        Map<String, Triple<LocalDateTime, String, String>> locationMap = Map.of("123", pair123, "456", pair456);
        when(properties.getLineRefs()).thenReturn("789");
        objectUnderTest = new ScheduledTasks(properties, applicationContext, Map.of());
        objectUnderTest.pushUpdates();
        verify(webSocketController, never()).pushUpdateToLineRef(anyString(), any());
    }

    @Test
    void givenLineRefValueShouldPushBusPositionUpdate() {
        Map<String, Triple<LocalDateTime, String, String>> locationMap = Map.of("123", pair123, "456", pair456);
        when(properties.getLineRefs()).thenReturn("123,456");
        when(applicationContext.getBean(FbisWebSocketController.class)).thenReturn(webSocketController);
        objectUnderTest = new ScheduledTasks(properties, applicationContext, locationMap);
        objectUnderTest.pushUpdates();
        verify(webSocketController).pushUpdateToLineRef(eq("123"), eq(pair123));
        verify(webSocketController).pushUpdateToLineRef(eq("456"), eq(pair456));
    }
}
