package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.AnalyzeKafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.impl.HubEventServiceImpl;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {
    private final AnalyzeKafkaProperties props;
    private final HubEventServiceImpl service;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Consumer<String, HubEventAvro> consumer;

    @Override
    public void run() {
        AnalyzeKafkaProperties.ConsumerConfig cfg = props.getHubConsumer();

        log.info("START HubEventProcessor: topic={}, pollTimeout={}, groupId={}",
                cfg.getTopic(), cfg.getPollTimeout(), cfg.getProperties().getProperty("group.id"));

        consumer = new KafkaConsumer<>(cfg.getProperties());

        try {
            consumer.subscribe(List.of(cfg.getTopic()));

            while (running.get()) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(cfg.getPollTimeout());

                records.forEach(record -> {
                    HubEventAvro event = record.value();
                    log.info("HubEvent: key={}, partition={}, offset={}, hubId={}, payloadType={}",
                            record.key(), record.partition(), record.offset(),
                            event.getHubId(),
                            event.getPayload() == null ? "null" : event.getPayload().getClass().getSimpleName());

                    service.handle(event);
                });

            }
        } catch (WakeupException e) {
            log.info("HubEventProcessor wakeup - stopping");
        } catch (Exception e) {
            log.error("HubEventProcessor failed", e);
        } finally {
            try {
                if (consumer != null) consumer.close();
                log.info("HubEventProcessor consumer closed");
            } catch (Exception e) {
                log.error("Error closing HubEventProcessor consumer", e);
            }
        }
    }

    public void stop() {
        log.info("STOP HubEventProcessor requested");
        running.set(false);
        if (consumer != null) consumer.wakeup();
    }
}

