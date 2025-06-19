package com.flatshire.fbis;

import com.flatshire.fbis.service.BodsService;
import com.flatshire.fbis.service.BodsServiceImpl;
import com.flatshire.fbis.helpers.csv.BusRouteReader;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
@Import({BodsServiceImpl.class, FbisProperties.class, BusRouteReader.class})
@ActiveProfiles("test")
@TestPropertySource(properties = "app.scheduling.enable=false")
class FbisApplicationTest {

    @MockBean
    private RestTemplateBuilder restTemplateBuilder;
    @Autowired
    private BodsService bodsService;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
    }

    @Test
    void BodsServiceWorks() {
		Triple<LocalDateTime, String, String> result = bodsService.readPositionFromDataFeed("1");
		assertThat(result, notNullValue());
    }

}
