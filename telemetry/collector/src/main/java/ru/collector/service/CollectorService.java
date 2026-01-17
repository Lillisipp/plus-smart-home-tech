package ru.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.collector.configuration.KafkaConfig;
import ru.collector.model.DeviceAction;
import ru.collector.model.HubEvent;
import ru.collector.model.ScenarioCondition;
import ru.collector.model.SensorEvent;
import ru.collector.model.hubs.DeviceAddedEvent;
import ru.collector.model.hubs.DeviceRemovedEvent;
import ru.collector.model.hubs.ScenarioAddedEvent;
import ru.collector.model.hubs.ScenarioRemovedEvent;
import ru.collector.model.sensors.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectorService {
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final KafkaConfig kafkaConfig;

    public void collectSensorEvent(SensorEvent event) {
        if (event == null || event.getEventType() == null) {
            log.warn("Ignored SENSOR event: event/type is null");
            return;
        }
        String topic = kafkaConfig.getProducer().topic(KafkaConfig.TopicType.SENSORS_EVENTS);
        String key = event.getHubId();

        log.info("Kafka SEND SENSOR: topic={}, key={}, type={}, id={}", topic, key, event.getEventType(), event.getId());

        final SensorEventAvro avro;
        try {
            avro = toSensorEventAvro(event);
        } catch (Exception e) {
            log.error("Ignored SENSOR event due to mapping error: hubId={}, id={}, type={}",
                    String.valueOf(event.getHubId()),
                    String.valueOf(event.getId()),
                    event.getEventType(),
                    e);
            return;
        }
        kafkaTemplate.send(topic, key, avro)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka SENSOR send FAILED: topic={}, key={}, type={}, id={}",
                                topic, key, event.getEventType(), event.getId(), ex);
                    } else {
                        var meta = result.getRecordMetadata();
                        log.info("Kafka HUB send OK: topic={}, key={}, partition={}, offset={}",
                                meta.topic(), key, meta.partition(), meta.offset()); // ИЗМЕНЕНИЕ

                    }
                });
    }


    public void collectHubEvent(HubEvent event) {
        if (event == null || event.getEventType() == null) {
            log.warn("Ignored HUB event: event/type is null");
            return;
        }

        String topic = kafkaConfig.getProducer().topic(KafkaConfig.TopicType.HUBS_EVENTS);
        String key = event.getHubId();
        log.info("Kafka SEND HUB: topic={}, key={}, type={}", topic, key, event.getEventType());

        final HubEventAvro avro;
        try {
            avro = toHubEventAvro(event);
        } catch (Exception e) {
            log.error("Ignored HUB event due to mapping error: hubId={},  type={}",
                    String.valueOf(event.getHubId()),
                    event.getEventType(),
                    e);
            return;
        }

        kafkaTemplate.send(topic, key, avro)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka HUB send FAILED: topic={}, key={}, type={}", topic, key, event.getEventType(), ex);
                    } else {
                        var meta = result.getRecordMetadata();
                        log.info("Kafka HUB send OK:: topic={}, key={}, type={}", topic, key, event.getEventType());
                    }
                });
    }


    private SensorEventAvro toSensorEventAvro(SensorEvent event) {
        Object payload = switch (event.getEventType()) {
            case LIGHT_SENSOR_EVENT -> mapLightPayload((LightSensorEvent) event);
            case MOTION_SENSOR_EVENT -> mapMotionPayload((MotionSensorEvent) event);
            case SWITCH_SENSOR_EVENT -> mapSwitchPayload((SwitchSensorEvent) event);
            case TEMPERATURE_SENSOR_EVENT -> mapTemperaturePayload((TemperatureSensorEvent) event);
            case CLIMATE_SENSOR_EVENT -> mapClimatePayload((ClimateSensorEvent) event);
        };

        SensorEventAvro avro = new SensorEventAvro();
        avro.setId(event.getId());
        avro.setHubId(event.getHubId());
        avro.setTimestamp(event.getTimestamp() == null ? Instant.now() : event.getTimestamp());
        avro.setPayload(payload);

        return avro;
    }

    private HubEventAvro toHubEventAvro(HubEvent event) {
        Object payload = switch (event.getEventType()) {
            case DEVICE_ADDED -> mapDeviceAdded((DeviceAddedEvent) event);
            case DEVICE_REMOVED -> mapDeviceRemovedPayload((DeviceRemovedEvent) event);
            case SCENARIO_ADDED -> mapScenarioAddedPayload((ScenarioAddedEvent) event);
            case SCENARIO_REMOVED -> mapScenarioRemovedPayload((ScenarioRemovedEvent) event);
        };
        HubEventAvro avro = new HubEventAvro();
        avro.setHubId(event.getHubId());
        avro.setPayload(payload);
        avro.setTimestamp(event.getTimestamp() == null ? Instant.now() : event.getTimestamp());

        return avro;
    }


    private LightSensorAvro mapLightPayload(LightSensorEvent e) {
        LightSensorAvro p = new LightSensorAvro();
        p.setLinkQuality(e.getLinkQuality());
        p.setLuminosity(e.getLuminosity());
        return p;
    }

    private MotionSensorAvro mapMotionPayload(MotionSensorEvent e) {
        MotionSensorAvro p = new MotionSensorAvro();
        p.setLinkQuality(e.getLinkQuality());
        p.setMotion(e.isMotion());
        p.setVoltage(e.getVoltage());
        return p;
    }

    private SwitchSensorAvro mapSwitchPayload(SwitchSensorEvent e) {
        SwitchSensorAvro p = new SwitchSensorAvro();
        p.setState(e.isState());
        return p;
    }

    private TemperatureSensorAvro mapTemperaturePayload(TemperatureSensorEvent e) {
        TemperatureSensorAvro p = new TemperatureSensorAvro();
        p.setTemperatureC(e.getTemperatureC());
        p.setTemperatureF(e.getTemperatureF());
        return p;
    }

    private ClimateSensorAvro mapClimatePayload(ClimateSensorEvent e) {
        ClimateSensorAvro p = new ClimateSensorAvro();
        p.setTemperatureC(e.getTemperatureC());
        p.setHumidity(e.getHumidity());
        p.setCo2Level(e.getCo2Level());
        return p;
    }

    private DeviceAddedEventAvro mapDeviceAdded(DeviceAddedEvent e) {
                log.info("Mapping DeviceAddedEvent to Avro: hubId={}, id={}, eventType={}, deviceType={}",
                        e.getHubId(),                 // если есть в HubEvent
                        e.getId(),
                        e.getEventType(),
                        e.getDeviceType());

                DeviceAddedEventAvro p = new DeviceAddedEventAvro();
        p.setId(e.getId());
        p.setType(DeviceTypeAvro.valueOf(e.getDeviceType().name()));
        return p;
    }

    private DeviceRemovedEventAvro mapDeviceRemovedPayload(DeviceRemovedEvent e) {
        DeviceRemovedEventAvro p = new DeviceRemovedEventAvro();
        p.setId(e.getId());
        return p;
    }

    private ScenarioAddedEventAvro mapScenarioAddedPayload(ScenarioAddedEvent e) {
        ScenarioAddedEventAvro p = new ScenarioAddedEventAvro();

        List<ScenarioConditionAvro> conditions = e.getConditions().stream()
                .map(this::mapScenarioCondition)
                .collect(Collectors.toList());

        List<DeviceActionAvro> actions = e.getActions().stream()
                .map(this::mapDeviceAction)
                .collect(Collectors.toList());

        p.setName(e.getName());
        p.setConditions(conditions);
        p.setActions(actions);
        return p;
    }

    private ScenarioConditionAvro mapScenarioCondition(ScenarioCondition c) {
        ScenarioConditionAvro a = new ScenarioConditionAvro();
        a.setSensorId(c.getSensorId());
        a.setType(ConditionTypeAvro.valueOf(c.getType().name()));
        a.setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()));

        if (c.getIntValue() != null && c.getBooleanValue() != null) {
            log.warn("ScenarioCondition has BOTH intValue and booleanValue set. sensorId={}, type={}",
                    c.getSensorId(), c.getType());
        }
        a.setValue(c.getIntValue() != null ? c.getIntValue() : c.getBooleanValue());
        return a;
    }

    private DeviceActionAvro mapDeviceAction(DeviceAction d) {
        DeviceActionAvro a = new DeviceActionAvro();
        a.setSensorId(d.getSensorId());
        a.setType(ActionTypeAvro.valueOf(d.getType().name()));
        a.setValue(d.getValue()); // Integer или null
        return a;
    }

    private ScenarioRemovedEventAvro mapScenarioRemovedPayload(ScenarioRemovedEvent e) {
        ScenarioRemovedEventAvro p = new ScenarioRemovedEventAvro();
        p.setName(e.getName());
        return p;
    }
}


