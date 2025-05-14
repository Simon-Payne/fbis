package com.flatshire.fbis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConditionalOnProperty(
		value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true
)
@SpringBootApplication
@EnableScheduling
public class FbisApplication {

	public static void main(String[] args) {
		SpringApplication.run(FbisApplication.class, args);
	}

}
