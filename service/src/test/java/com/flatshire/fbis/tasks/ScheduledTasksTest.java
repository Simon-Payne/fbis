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
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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

    private static final ImmutableTriple<LocalDateTime, String, String> pair123 = ImmutableTriple.of(LocalDateTime.of(LocalDate.of(2025, 6, 1), LocalTime.of(10, 30, 0, 0)), "1234567890.1", "2345678901.2");
    private static final ImmutableTriple<LocalDateTime, String, String> pair456 = ImmutableTriple.of(LocalDateTime.of(LocalDate.of(2025, 6, 1), LocalTime.of(10, 35, 0, 0)), "4234567890.1", "4345678901.2");

    @Test
    void givenLineRefStringShouldReadDataFeedsForEachValue() {
        when(properties.getLineRefs()).thenReturn("123,456");
        when(applicationContext.getBean(FbisWebSocketController.class)).thenReturn(webSocketController);
        when(webSocketController.readFeedForLineRef(anyString())).thenReturn(pair123).thenReturn(pair456);
        Map<String, Triple<LocalDateTime, String, String>> locationMap = Map.of("123", pair123, "456", pair456);
        objectUnderTest = new ScheduledTasks(properties, applicationContext, locationMap);
        objectUnderTest.readDataFeeds();
        Map<String, Triple<LocalDateTime, String, String>> pairMap = objectUnderTest.reportMapContents();
        assertThat(pairMap.keySet(), equalTo(Set.of("123", "456")));
        assertThat(pairMap.get("123"), equalTo(pair123));
        assertThat(pairMap.get("456"), equalTo(pair456));
    }

    @Test
    void givenLineRefValueShouldPushBusPositionUpdate() {
        Map<String, Triple<LocalDateTime, String, String>> locationMap = Map.of("123", pair123, "456", pair456);
        when(properties.getLineRefs()).thenReturn("123,456");
        when(applicationContext.getBean(FbisWebSocketController.class)).thenReturn(webSocketController);
        doNothing().when(webSocketController).pushUpdateToLineRef(anyString(), any());
        objectUnderTest = new ScheduledTasks(properties, applicationContext, locationMap);
        objectUnderTest.pushUpdates();
        verify(webSocketController).pushUpdateToLineRef(eq("123"), eq(pair123));
        verify(webSocketController).pushUpdateToLineRef(eq("456"), eq(pair456));
    }
}
