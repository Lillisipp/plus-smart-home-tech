package ru.collector.model.hubs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import ru.collector.model.DeviceAction;
import ru.collector.model.HubEvent;
import ru.collector.model.ScenarioCondition;
import ru.collector.model.enams.HubsEventType;

import java.util.List;

@Setter
@Getter
public class ScenarioAddedEvent extends HubEvent {
    @NotBlank
    private String name;

    @Valid
    @NotEmpty
    private List<ScenarioCondition> conditions; // условия

    @Valid
    @NotEmpty
    private List<DeviceAction> actions;

    @Override
    public HubsEventType getEventType() {
        return HubsEventType.SCENARIO_ADDED;
    }
}
