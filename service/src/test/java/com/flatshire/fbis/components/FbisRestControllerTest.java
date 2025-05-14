package com.flatshire.fbis.components;

import com.flatshire.fbis.domain.BusInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FbisRestController.class)
class FbisRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BodsServiceImpl bodsService;

    @Test
    void shouldHandleBadRequest() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/operators/{operatorRef}/buses", "123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetOperatorBuses() throws Exception {
        String operatorRef = "ABCD";
        String bus1 = "1";
        String bus2 = "2";
        List<BusInfo> buses = List.of(BusInfo.of(operatorRef, bus1),
                BusInfo.of(operatorRef, bus2));
        when(bodsService.readBusInfo(anyString())).thenReturn(buses);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/operators/{operatorRef}/buses", operatorRef)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].operatorRef").value(operatorRef))
                .andExpect(jsonPath("$[0].lineRef").value(bus1))
                .andExpect(jsonPath("$[1].operatorRef").value(operatorRef));
    }

}
