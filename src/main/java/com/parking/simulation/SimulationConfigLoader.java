package com.parking.simulation;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Properties;

public final class SimulationConfigLoader {
    private SimulationConfigLoader() {
    }

    public static SimulationConfig loadFromResource(String resourcePath) {
        Properties properties = new Properties();
        try (InputStream inputStream = SimulationConfigLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load simulation config: " + resourcePath, exception);
        }

        return new SimulationConfig(
                properties.getProperty("scenarioName", "sample-simulation"),
                LocalDateTime.parse(Objects.requireNonNull(properties.getProperty("startTime"))),
                Integer.parseInt(properties.getProperty("totalSteps")),
                Integer.parseInt(properties.getProperty("stepMinutes")),
                Double.parseDouble(properties.getProperty("arrivalProbability")),
                Integer.parseInt(properties.getProperty("maxArrivalsPerStep")),
                Integer.parseInt(properties.getProperty("minParkingMinutes")),
                Integer.parseInt(properties.getProperty("maxParkingMinutes")),
                Long.parseLong(properties.getProperty("seed"))
        );
    }
}
