package ru.yandex.practicum.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "aggregator.kafka")
public class AggregatorKafkaProperties {
    @NotBlank
    private String bootstrapServers;
    @NotBlank
    private String sensorsTopic = "telemetry.sensors.v1";

    @NotBlank
    private String snapshotsTopic = "telemetry.snapshots.v1";

    @NotBlank
    private String consumerGroupId = "aggregator";

    @NotNull
    private Duration pollTimeout = Duration.ofMillis(500);

    private Map<String, String> consumer = new HashMap<>();
    private Map<String, String> producer = new HashMap<>();
}
