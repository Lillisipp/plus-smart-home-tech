package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.AnalyzeKafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.impl.SnapshotAnalysisImpl;


import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor implements Runnable {

    private final AnalyzeKafkaProperties props;
    private final SnapshotAnalysisImpl snapshotAnalysisService;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private Consumer<String, SensorsSnapshotAvro> consumer;


    public void run() {
        AnalyzeKafkaProperties.ConsumerConfig cfg = props.getSnapshotConsumer();

        log.info("START SnapshotProcessor: topic={}, pollTimeout={}, groupId={}",
                cfg.getTopic(), cfg.getPollTimeout(), cfg.getProperties().getProperty("group.id"));

        consumer = new KafkaConsumer<>(cfg.getProperties());

        try {
            consumer.subscribe(List.of(cfg.getTopic()));

            while (running.get()) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(cfg.getPollTimeout());

                if (records.isEmpty()) continue;

                records.forEach(record -> {
                    SensorsSnapshotAvro snapshot = record.value();
                    log.info("Snapshot: key={}, partition={}, offset={}, hubId={}, ts={}",
                            record.key(), record.partition(), record.offset(),
                            snapshot.getHubId(), snapshot.getTimestamp());

                    snapshotAnalysisService.handleSnapshot(snapshot);
                });

                consumer.commitSync();
                log.info("SnapshotProcessor commitSync done");
            }
        } catch (WakeupException e) {
            log.info("SnapshotProcessor wakeup - stopping");
        } catch (Exception e) {
            log.error("SnapshotProcessor failed", e);
        } finally {
            try {
                if (consumer != null) consumer.close();
                log.info("SnapshotProcessor consumer closed");
            } catch (Exception e) {
                log.error("Error closing SnapshotProcessor consumer", e);
            }
        }
    }

    public void stop() {
        log.info("STOP SnapshotProcessor requested");
        running.set(false);
        if (consumer != null) consumer.wakeup();
    }
}
