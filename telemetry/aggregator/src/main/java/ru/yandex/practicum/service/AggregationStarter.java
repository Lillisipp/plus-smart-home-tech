package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.AggregatorKafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final Consumer<String, SensorEventAvro> consumer;
    private final Producer<String, SensorsSnapshotAvro> producer;
    private final AggregatorKafkaProperties props;

    // hubId -> snapshot
    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(props.getSensorsTopic()));

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(props.getPollTimeout());
                if (records.isEmpty()) continue;

                records.forEach(r -> {
                    SensorEventAvro event = r.value();
                    if (event == null) return;

                    updateState(event).ifPresent(snapshot -> {
                        String key = snapshot.getHubId().toString();
                        producer.send(new ProducerRecord<>(props.getSnapshotsTopic(), key, snapshot));
                    });
                });

                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                consumer.close();
                producer.close();
            }
        }
    }

    Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        return Optional.empty();
    }
}
