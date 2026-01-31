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

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final Consumer<String, SensorEventAvro> consumer;
    private final Producer<String, SensorsSnapshotAvro> producer;
    private final AggregatorKafkaProperties props;

    private final SnapshotService snapshotService; // [ИЗМЕНЕНИЕ] используем сервис

    public void start() {
        log.info("START AggregationStarter: sensorsTopic={}, snapshotsTopic={}, groupId={}",
                props.getSensorsTopic(), props.getSnapshotsTopic(), props.getConsumerGroupId());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("ShutdownHook: consumer.wakeup()");
            consumer.wakeup();
        }));

        try {
            consumer.subscribe(List.of(props.getSensorsTopic()));
            log.info("Subscribed to {}", props.getSensorsTopic());

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(props.getPollTimeout());
                if (records.isEmpty()) {
                    continue;
                }

                log.debug("Polled {} records", records.count());

                records.forEach(r -> {
                    SensorEventAvro event = r.value();
                    if (event == null) {
                        log.warn("Skip null event: topic={}, partition={}, offset={}",
                                r.topic(), r.partition(), r.offset());
                        return;
                    }

                    snapshotService.updateState(event).ifPresent(snapshot -> {
                        String key = snapshot.getHubId().toString();
                        producer.send(new ProducerRecord<>(props.getSnapshotsTopic(), key, snapshot));
                        log.info("Produced snapshot: topic={}, key={}, snapshotTs={}",
                                props.getSnapshotsTopic(), key, snapshot.getTimestamp());
                    });
                });

                consumer.commitAsync((offsets, ex) -> {
                    if (ex != null) {
                        log.error("Commit failed: {}", offsets, ex);
                    } else {
                        log.debug("Commit ok: {}", offsets);
                    }
                });
            }
        } catch (WakeupException e) {
            log.info("WakeupException -> stopping");
        } catch (Exception e) {
            log.error("Aggregation loop error", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } catch (Exception e) {
                log.error("Shutdown flush/commit error", e);
            } finally {
                consumer.close();
                producer.close();
                log.info("Closed consumer/producer");
            }
        }
    }
}
