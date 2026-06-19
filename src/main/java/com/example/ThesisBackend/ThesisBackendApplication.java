package com.example.ThesisBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class ThesisBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThesisBackendApplication.class, args);
	}

}
