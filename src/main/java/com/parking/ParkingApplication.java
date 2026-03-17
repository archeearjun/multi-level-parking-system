package com.parking;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ParkingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParkingApplication.class, args);
    }

    @Bean
    OpenAPI parkingOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Multi-Level Parking System API")
                .description("Spring Boot parking management API with optimized allocation, EV prioritization, analytics, simulation, and benchmark endpoints.")
                .version("v1")
                .contact(new Contact().name("Portfolio Project"))
                .license(new License()
                        .name("MIT")
                        .url("https://opensource.org/licenses/MIT")));
    }
}
