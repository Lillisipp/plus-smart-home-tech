package ru.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.collector.configuration.KafkaConfig;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectorService {
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final KafkaConfig kafkaConfig;

    public void collectSensorEvent(SensorEventProto event) {
        log.info("ENTER collectSensorEvent(proto): payloadCase={}, hubId={}, id={}, timestamp={}",
                (event == null ? null : event.getPayloadCase()),
                (event == null ? null : event.getHubId()),
                (event == null ? null : event.getId()),
                (event == null ? null : event.getTimestamp())
        );
        if (event == null) {
            log.warn("Ignored SENSOR event: event/type is null");
            return;
        }
        if (event.getPayloadCase() == SensorEventProto.PayloadCase.PAYLOAD_NOT_SET) {
            log.warn("Ignored SENSOR event: payload is not set. hubId={}, id={}", event.getHubId(), event.getId());
            return;
        }

        String topic = kafkaConfig.getProducer().topic(KafkaConfig.TopicType.SENSORS_EVENTS);
        String key = event.getHubId();

        log.info("SENSOR routing: topic={}, key={}", topic, key);

        final SensorEventAvro avro;
        try {
            avro = toSensorEventAvro(event);
        } catch (Exception e) {
            log.error("Ignored SENSOR event due to mapping error: hubId={}, id={}, payloadCase={}",
                    event.getHubId(),
                    event.getId(),
                    event.getPayloadCase(),
                    e);
            return;
        }
        Instant ts = Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos());
        sendToKafkaAndWait(topic, key, avro, ts, "SENSOR");
    }

    public void collectHubEvent(HubEventProto event) {
        if (event == null || event.getPayloadCase() == HubEventProto.PayloadCase.PAYLOAD_NOT_SET) {
            log.warn("Ignored HUB event: event/payload is null or not set");
            return;
        }

        String topic = kafkaConfig.getProducer().topic(KafkaConfig.TopicType.HUBS_EVENTS);
        String key = event.getHubId();
        log.info("Kafka SEND HUB: topic={}, key={}, type={}", topic, key, event.getPayloadCase());

        final HubEventAvro avro;
        try {
            avro = toHubEventAvro(event);
        } catch (Exception e) {
            log.error("Ignored HUB event due to mapping error: hubId={},  type={}",
                    String.valueOf(event.getHubId()),
                    event.getPayloadCase(),
                    e);
            return;
        }
        Instant ts = Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos());
        sendToKafkaAndWait(topic, key, avro, ts, "HUB");

    }

    private SensorEventAvro toSensorEventAvro(SensorEventProto event) {
        Object payload = switch (event.getPayloadCase()) {
            case MOTION_SENSOR_EVENT -> mapMotionPayload(event.getMotionSensorEvent());
            case TEMPERATURE_SENSOR_EVENT -> mapTemperaturePayload(event.getTemperatureSensorEvent());
            case LIGHT_SENSOR_EVENT -> mapLightPayload(event.getLightSensorEvent());
            case CLIMATE_SENSOR_EVENT -> mapClimatePayload(event.getClimateSensorEvent());
            case SWITCH_SENSOR_EVENT -> mapSwitchPayload(event.getSwitchSensorEvent());
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("payload not set");
        };

        SensorEventAvro avro = new SensorEventAvro();
        avro.setId(event.getId());
        avro.setHubId(event.getHubId());
        avro.setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()));
        avro.setPayload(payload);

        return avro;
    }

    private HubEventAvro toHubEventAvro(HubEventProto event) {
        Object payload = switch (event.getPayloadCase()) {
            case DEVICE_ADDED -> mapDeviceAdded(event.getDeviceAdded());
            case DEVICE_REMOVED -> mapDeviceRemovedPayload(event.getDeviceRemoved());
            case SCENARIO_ADDED -> mapScenarioAddedPayload(event.getScenarioAdded());
            case SCENARIO_REMOVED -> mapScenarioRemovedPayload(event.getScenarioRemoved());
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("payload not set");
        };
        HubEventAvro avro = new HubEventAvro();
        avro.setHubId(event.getHubId());
        avro.setPayload(payload);
        avro.setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()));
        return avro;
    }

    private LightSensorAvro mapLightPayload(LightSensorProto e) {
        LightSensorAvro p = new LightSensorAvro();
        p.setLinkQuality(e.getLinkQuality());
        p.setLuminosity(e.getLuminosity());
        return p;
    }

    private MotionSensorAvro mapMotionPayload(MotionSensorProto e) {
        MotionSensorAvro p = new MotionSensorAvro();
        p.setLinkQuality(e.getLinkQuality());
        p.setMotion(e.getMotion());
        p.setVoltage(e.getVoltage());
        return p;
    }

    private SwitchSensorAvro mapSwitchPayload(SwitchSensorProto e) {
        SwitchSensorAvro p = new SwitchSensorAvro();
        p.setState(e.getState());
        return p;
    }

    private TemperatureSensorAvro mapTemperaturePayload(TemperatureSensorProto e) {
        TemperatureSensorAvro p = new TemperatureSensorAvro();
        p.setTemperatureC(e.getTemperatureC());
        p.setTemperatureF(e.getTemperatureF());
        return p;
    }

    private ClimateSensorAvro mapClimatePayload(ClimateSensorProto e) {
        ClimateSensorAvro p = new ClimateSensorAvro();
        p.setTemperatureC(e.getTemperatureC());
        p.setHumidity(e.getHumidity());
        p.setCo2Level(e.getCo2Level());
        return p;
    }

    private DeviceAddedEventAvro mapDeviceAdded(DeviceAddedEventProto e) {
        log.info("Mapping DeviceAddedEventProto to Avro: id={}, type={}",
                e.getId(),
                e.getType());

        DeviceAddedEventAvro p = new DeviceAddedEventAvro();
        p.setId(e.getId());
        p.setType(DeviceTypeAvro.valueOf(e.getType().name()));
        return p;
    }

    private DeviceRemovedEventAvro mapDeviceRemovedPayload(DeviceRemovedEventProto e) {
        DeviceRemovedEventAvro p = new DeviceRemovedEventAvro();
        p.setId(e.getId());
        return p;
    }

    private ScenarioAddedEventAvro mapScenarioAddedPayload(ScenarioAddedEventProto e) {
        ScenarioAddedEventAvro p = new ScenarioAddedEventAvro();

        List<ScenarioConditionAvro> conditions = e.getConditionList().stream()
                .map(this::mapScenarioCondition)
                .collect(Collectors.toList());

        List<DeviceActionAvro> actions = e.getActionList().stream()
                .map(this::mapDeviceAction)
                .collect(Collectors.toList());

        p.setName(e.getName());
        p.setConditions(conditions);
        p.setActions(actions);
        return p;
    }

    private ScenarioConditionAvro mapScenarioCondition(ScenarioConditionProto c) {
        ScenarioConditionAvro a = new ScenarioConditionAvro();
        a.setSensorId(c.getSensorId());
        a.setType(ConditionTypeAvro.valueOf(c.getType().name()));
        a.setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()));

        switch (c.getValueCase()) {
            case BOOL_VALUE -> a.setValue(c.getBoolValue());
            case INT_VALUE -> a.setValue(c.getIntValue());
            case VALUE_NOT_SET -> {
                log.warn("ScenarioCondition has no value set. sensorId={}, type={}",
                        c.getSensorId(), c.getType());
                throw new IllegalArgumentException("ScenarioConditionProto value is not set: sensorId=" + c.getSensorId());
            }
        }
        return a;
    }

    private DeviceActionAvro mapDeviceAction(DeviceActionProto d) {
        DeviceActionAvro a = new DeviceActionAvro();
        a.setSensorId(d.getSensorId());
        a.setType(ActionTypeAvro.valueOf(d.getType().name()));
        a.setValue(d.hasValue() ? d.getValue() : null); // Integer или null
        return a;
    }

    private ScenarioRemovedEventAvro mapScenarioRemovedPayload(ScenarioRemovedEventProto e) {
        ScenarioRemovedEventAvro p = new ScenarioRemovedEventAvro();
        p.setName(e.getName());
        return p;
    }

    private void sendToKafkaAndWait(String topic, String key, Object value, Instant timestamp, String label) {
        if (topic == null || topic.isBlank()) {
            log.error("Kafka {} SEND ignored: topic is null/blank, key={}", label, key);
            throw new IllegalStateException("Kafka topic is null/blank for " + label);
        }
        if (key == null || key.isBlank()) {
            log.warn("Kafka {} SEND: key is null/blank (message will be sent with null/blank key). topic={}", label, topic);
        }
        if (value == null) {
            log.error("Kafka {} SEND ignored: value is null. topic={}, key={}", label, topic, key);
            throw new IllegalArgumentException("Kafka value is null for " + label);
        }
        if (timestamp == null) {
            log.warn("Kafka {} SEND: timestamp is null -> using Instant.now(). topic={}, key={}", label, topic, key);
            timestamp = Instant.now();
        }

        long recordTs = timestamp.toEpochMilli();

        log.info("Kafka {} SEND: topic={}, key={}, recordTs={}, valueClass={}",
                label, topic, key, recordTs, value.getClass().getName());
        try {
            ProducerRecord<Object, Object> record = new ProducerRecord<>(
                    topic,
                    null,
                    recordTs,
                    key,
                    value
            );

            long start = System.nanoTime();

            SendResult<Object, Object> result = kafkaTemplate
                    .send(record)
                    .get(3, TimeUnit.SECONDS);

            long tookMs = (System.nanoTime() - start) / 1_000_000;
            var meta = result.getRecordMetadata();
            log.info("Kafka {} ACK: topic={}, partition={}, offset={}, key={}, tookMs={}",
                    label, meta.topic(), meta.partition(), meta.offset(), key, tookMs);

        } catch (TimeoutException e) {
            log.error("Kafka {} SEND TIMEOUT: topic={}, key={}", label, topic, key, e);
            throw new RuntimeException("Kafka send timeout (" + label + ")", e);

        } catch (InterruptedException e) {
            log.error("Kafka {} SEND INTERRUPTED: topic={}, key={}", label, topic, key, e);
            throw new RuntimeException("Kafka send interrupted (" + label + ")", e);

        } catch (ExecutionException e) {
            log.error("Kafka {} SEND FAILED (ExecutionException): topic={}, key={}, cause={}",
                    label, topic, key, (e.getCause() == null ? null : e.getCause().toString()), e);
            throw new RuntimeException("Kafka send failed (" + label + ")", e);

        } catch (Exception e) {
            log.error("Kafka {} SEND FAILED: topic={}, key={}", label, topic, key, e);
            throw new RuntimeException("Kafka send failed (" + label + ")", e);
        }
    }
}


