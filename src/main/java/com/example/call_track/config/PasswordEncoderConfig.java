package com.example.call_track.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder(
            @Value("${password.encoder.argon2.salt-length:32}") int saltLength,
            @Value("${password.encoder.argon2.hash-length:32}") int hashLength,
            @Value("${password.encoder.argon2.parallelism:4}") int parallelism,
            @Value("${password.encoder.argon2.memory:65536}") int memory,
            @Value("${password.encoder.argon2.iterations:3}") int iterations) {

        return new Argon2PasswordEncoder(
                saltLength,
                hashLength,
                parallelism,
                memory,
                iterations);
    }
}