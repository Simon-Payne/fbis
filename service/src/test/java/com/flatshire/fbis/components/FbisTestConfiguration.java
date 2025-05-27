package com.flatshire.fbis.components;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@TestConfiguration
public class FbisTestConfiguration {

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("user")
                .password("userPass")
                .roles("USER")
                .build());
        manager.createUser(User.withUsername("admin")
                .password("adminPass")
                .roles("USER", "ADMIN")
                .build());
        return manager;
    }
}