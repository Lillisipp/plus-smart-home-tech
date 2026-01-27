package ru.collector.configuration;

import kafka.serializer.GeneralAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableConfigurationProperties(KafkaConfig.class)
public class KafkaProducerConfiguration {

    @Bean
    public ProducerFactory<Object, Object> producerFactory(KafkaConfig kafkaConfig) {
        Map<String, Object> props = new HashMap<>(kafkaConfig.getProducer().getProperties());

        log.info("Kafka producer properties: {}", props);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate(ProducerFactory<Object, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
