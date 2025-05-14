package com.flatshire.fbis.components;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BodsBeans {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
