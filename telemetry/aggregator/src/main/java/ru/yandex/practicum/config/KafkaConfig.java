package ru.yandex.practicum.config;

import kafka.serializer.GeneralAvroSerializer;
import kafka.serializer.SensorEventDeserializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Properties;

@Slf4j
@Getter
@RequiredArgsConstructor
@Configuration
public class KafkaConfig {
    private final AggregatorKafkaProperties props;

    @Bean
    public Consumer<String, SensorEventAvro> sensorEventsConsumer() {
        Properties c = new Properties();

        c.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        c.put(ConsumerConfig.GROUP_ID_CONFIG, props.getConsumerGroupId());

        c.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        c.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        c.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        c.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class.getName());

        // доп настройки из yml
        props.getConsumer().forEach(c::put);

        return new KafkaConsumer<>(c);
    }

    @Bean
    public Producer<String, SensorsSnapshotAvro> snapshotsProducer() {
        Properties p = new Properties();

        p.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());

        props.getProducer().forEach(p::put);

        return new KafkaProducer<>(p);
    }
}
