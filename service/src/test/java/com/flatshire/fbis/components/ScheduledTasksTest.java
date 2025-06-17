package com.flatshire.fbis.components;

import com.flatshire.fbis.FbisProperties;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

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

    ImmutablePair<String, String> pair123 = ImmutablePair.of("1234567890.1", "2345678901.2");
    ImmutablePair<String, String> pair456 = ImmutablePair.of("4234567890.1", "4345678901.2");

    @Test
    void givenLineRefStringShouldReadDataFeedsForEachValue() {
        when(properties.getLineRefs()).thenReturn("123,456");
        when(applicationContext.getBean(FbisWebSocketController.class)).thenReturn(webSocketController);
        when(webSocketController.readFeedForLineRef(anyString())).thenReturn(pair123).thenReturn(pair456);
        Map<String, Pair<String, String>> locationMap = Map.of("123", pair123, "456", pair456);
        objectUnderTest = new ScheduledTasks(properties, applicationContext, locationMap);
        objectUnderTest.readDataFeeds();
        Map<String, Pair<String, String>> pairMap = objectUnderTest.reportMapContents();
        assertThat(pairMap.keySet(), equalTo(Set.of("123", "456")));
        assertThat(pairMap.get("123"), equalTo(pair123));
        assertThat(pairMap.get("456"), equalTo(pair456));
    }

    @Test
    void givenLineRefValueShouldPushBusPositionUpdate() {
        Map<String, Pair<String, String>> locationMap = Map.of("123", pair123, "456", pair456);
        when(properties.getLineRefs()).thenReturn("123,456");
        when(applicationContext.getBean(FbisWebSocketController.class)).thenReturn(webSocketController);
        doNothing().when(webSocketController).pushUpdateToLineRef(anyString(), any());
        objectUnderTest = new ScheduledTasks(properties, applicationContext, locationMap);
        objectUnderTest.pushUpdates();
        verify(webSocketController).pushUpdateToLineRef(eq("123"), eq(pair123));
        verify(webSocketController).pushUpdateToLineRef(eq("456"), eq(pair456));
    }
}
