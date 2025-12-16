package com.example.call_track;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CallTrackApplication {
	public static void main(String[] args) {
		SpringApplication.run(CallTrackApplication.class, args);
	}
}
