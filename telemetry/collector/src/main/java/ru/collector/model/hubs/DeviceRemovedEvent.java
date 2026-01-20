package ru.collector.model.hubs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.collector.model.HubEvent;
import ru.collector.model.enams.HubsEventType;

@Setter
@Getter
public class DeviceRemovedEvent extends HubEvent {

    @NotBlank
    @NotNull
    private String id;

    @Override
    public HubsEventType getEventType() {
        return HubsEventType.DEVICE_REMOVED;
    }
}
