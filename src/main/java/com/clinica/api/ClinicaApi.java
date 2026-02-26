package com.clinica.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {
    ReactiveUserDetailsServiceAutoConfiguration.class
})
public class ClinicaApi {
    public static void main(String[] args) {
        SpringApplication.run(ClinicaApi.class, args);
    }
}