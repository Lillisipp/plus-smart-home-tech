package ru.yandex.practicum.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.enums.ConditionType;
import ru.yandex.practicum.service.SnapshotValueReader;

import java.util.Map;

@Component
@Slf4j
public class SnapshotValueReaderImpl implements SnapshotValueReader {
    @Override
    public Integer readValue(SensorsSnapshotAvro snapshot, String sensorId, ConditionType type) {
        if (snapshot == null || sensorId == null || type == null) return null;

        Map<String, SensorStateAvro> stateMap = snapshot.getSensorsState();
        if (stateMap == null) return null;

        SensorStateAvro state = stateMap.get(sensorId);
        if (state == null) return null;

        Object data = state.getData();
        if (data == null) return null;

        return switch (type) {
            case TEMPERATURE -> temperature(data);
            case HUMIDITY -> humidity(data);
            case CO2LEVEL -> co2(data);
            case LUMINOSITY -> luminosity(data);
            case MOTION -> motion(data);
            case SWITCH -> sw(data);
        };
    }

    private Integer temperature(Object data) {
        if (data instanceof ClimateSensorAvro c) return c.getTemperatureC();
        if (data instanceof TemperatureSensorAvro t) return t.getTemperatureC();
        return null;
    }

    private Integer humidity(Object data) {
        if (data instanceof ClimateSensorAvro c) return c.getHumidity();
        return null;
    }

    private Integer co2(Object data) {
        if (data instanceof ClimateSensorAvro c) return c.getCo2Level();
        return null;
    }

    private Integer luminosity(Object data) {
        if (data instanceof LightSensorAvro l) return l.getLuminosity();
        return null;
    }

    private Integer motion(Object data) {
        if (data instanceof MotionSensorAvro m) return m.getMotion() ? 1 : 0;
        return null;
    }

    private Integer sw(Object data) {
        if (data instanceof SwitchSensorAvro s) return s.getState() ? 1 : 0;
        return null;
    }
}