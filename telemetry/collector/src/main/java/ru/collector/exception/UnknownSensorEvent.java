package ru.collector.exception;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.collector.model.SensorEvent;
import ru.collector.model.enams.SensorEventType;

import java.util.LinkedHashMap;
import java.util.Map;

@Setter
@Getter
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnknownSensorEvent extends SensorEvent {
    private final Map<String, Object> rawPayload = new LinkedHashMap<>();

    @JsonAnySetter
    public void putRawField(String key, Object value) {
        rawPayload.put(key, value);
    }

    public void logAndIgnore() {
        log.warn("IGNORED unknown SENSOR event: typeRaw={}, hubId={}, id={}, timestamp={}, rawPayload={}",
                getTypeRaw(), getHubId(), getId(), getTimestamp(), rawPayload);
    }

    @Override
    public SensorEventType getEventType() {
        return null; // неизвестный тип → enum не определяем
    }
}
