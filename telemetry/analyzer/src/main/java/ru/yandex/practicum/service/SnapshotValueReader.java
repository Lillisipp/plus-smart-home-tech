package ru.yandex.practicum.service;

import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.model.enums.ConditionType;

public interface SnapshotValueReader {
    Integer readValue(SensorsSnapshotAvro snapshot, String sensorId, ConditionType type);
}
