package ru.collector.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "collector.kafka")
@Slf4j
public class KafkaConfig {
    private ProducerConfig producer = new ProducerConfig();

    @Setter
    @ToString
    @Getter
    public static class ProducerConfig {
        private Map<String, Object> properties = new HashMap<>();
        private Map<TopicType, String> topics = new HashMap<>();

        public String topic(TopicType type) {
            log.info("Kafka topics {} ", topics);
            String topicName = topics.get(type);
            if (hasText(topicName)) {
                return topicName;
            }
            throw new IllegalStateException("Kafka topic is not configured for: %s".formatted(type));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum TopicType {
        SENSORS_EVENTS("sensors-events"),
        HUBS_EVENTS("hubs-events");

        private final String yamlKey;
    }
}
