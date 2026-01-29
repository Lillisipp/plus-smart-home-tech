package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {

    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro eventAvro) {
        log.debug("ENTER updateState: eventAvro={}", eventAvro);
        if (eventAvro == null) {
            log.warn("updateState: eventAvro is null -> ignore");
            return Optional.empty();
        }
        if (eventAvro.getHubId() == null || eventAvro.getId() == null || eventAvro.getTimestamp() == null) {
            log.warn("updateState: missing fields hubId/id/timestamp -> ignore. hubId={}, id={}, ts={}",
                    eventAvro.getHubId(), eventAvro.getId(), eventAvro.getTimestamp());
            return Optional.empty();
        }

        String hubId = eventAvro.getHubId().toString();
        String sensorId = eventAvro.getId().toString();
        Instant eventTs = eventAvro.getTimestamp();

        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(hubId, k -> {
            SensorsSnapshotAvro s = new SensorsSnapshotAvro();
            s.setHubId(hubId);
            s.setTimestamp(eventTs);
            s.setSensorsState(new ConcurrentHashMap<>()); // внутри держим состояние датчиков
            log.info("Создан новый snapshot для hubId={}", hubId);
            return s;
        });

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        if (sensorsState == null) {
            sensorsState = new ConcurrentHashMap<>();
            snapshot.setSensorsState(sensorsState);
            log.warn("snapshot.sensorsState was null -> initialized. hubId={}", hubId);
        }
        SensorStateAvro oldState = sensorsState.get(sensorId);

        long newTs = eventAvro.getTimestamp().toEpochMilli();
        if (oldState != null && oldState.getTimestamp() != null) {
            Instant oldTs = oldState.getTimestamp();
            if (oldTs.isAfter(eventTs)) {
                log.debug("updateState: ignore старое событие. hubId={}, sensorId={}, oldTs={}, newTs={}",
                        hubId, sensorId, oldTs, newTs);
                return Optional.empty();
            }
            if (oldTs.equals(eventTs) && safeEquals(oldState.getData(), eventAvro.getPayload())) {
                log.debug("Ignore duplicate event. hubId={}, sensorId={}, ts={}", hubId, sensorId, eventTs);
                return Optional.empty();
            }
        }

        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(eventTs);
        newState.setData(eventAvro.getPayload());
        sensorsState.put(sensorId, newState);

        snapshot.setTimestamp(eventTs);
        log.info("Snapshot updated. hubId={}, sensorId={}, ts={}", hubId, sensorId, eventTs);

        return Optional.of(snapshot);
    }

    private static boolean safeEquals(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }
}



