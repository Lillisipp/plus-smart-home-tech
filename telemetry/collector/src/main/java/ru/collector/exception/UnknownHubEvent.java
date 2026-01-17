package ru.collector.exception;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.collector.model.HubEvent;
import ru.collector.model.enams.HubsEventType;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnknownHubEvent extends HubEvent {
    private final Map<String, Object> rawPayload = new LinkedHashMap<>();

    @JsonAnySetter
    public void putRawField(String key, Object value) {
        rawPayload.put(key, value);
    }

    public void logAndIgnore() {
        log.warn("IGNORED unknown HUB event: typeRaw={}, hubId={}, timestamp={}, rawPayload={}",
                getTypeRaw(), getHubId(), getTimestamp(), rawPayload);
    }

    @Override
    public HubsEventType getEventType() {
        return null;
    }
}
