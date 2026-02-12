package ru.yandex.practicum.config;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Properties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "analyzer.kafka")
public class AnalyzeKafkaProperties {

    private final ConsumerConfig snapshotConsumer;
    private final ConsumerConfig hubConsumer;

    @Setter
    @Getter
    @AllArgsConstructor
    public static class ConsumerConfig {

        private String topic;
        private Duration pollTimeout;
        private Properties properties;
    }
}
