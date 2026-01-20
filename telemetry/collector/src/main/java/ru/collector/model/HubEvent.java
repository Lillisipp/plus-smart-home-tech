package ru.collector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.collector.exception.UnknownHubEvent;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.collector.model.enams.HubsEventType;
import ru.collector.model.hubs.DeviceAddedEvent;
import ru.collector.model.hubs.DeviceRemovedEvent;
import ru.collector.model.hubs.ScenarioAddedEvent;
import ru.collector.model.hubs.ScenarioRemovedEvent;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@JsonSubTypes({
        @JsonSubTypes.Type( value = DeviceAddedEvent.class, name = "DEVICE_ADDED"),
        @JsonSubTypes.Type( value = DeviceRemovedEvent.class, name = "DEVICE_REMOVED"),
        @JsonSubTypes.Type( value = ScenarioAddedEvent.class, name = "SCENARIO_ADDED"),
        @JsonSubTypes.Type( value = ScenarioRemovedEvent.class, name = "SCENARIO_REMOVED")

})
@JsonTypeInfo( use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true,
        defaultImpl = UnknownHubEvent.class
)
public abstract class HubEvent {
    @NotBlank
    private String hubId;

    private Instant timestamp = Instant.now();

    @JsonProperty("type")
    private String typeRaw; // + сырой type из JSON

    public abstract HubsEventType getEventType();
}
