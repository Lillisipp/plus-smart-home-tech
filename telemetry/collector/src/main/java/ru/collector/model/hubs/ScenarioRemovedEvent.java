package ru.collector.model.hubs;

import lombok.Getter;
import lombok.Setter;
import ru.collector.model.HubEvent;
import ru.collector.model.enams.HubsEventType;

@Setter
@Getter
public class ScenarioRemovedEvent extends HubEvent {
    private String name;

    @Override
    public HubsEventType getEventType() {
        return HubsEventType.SCENARIO_REMOVED;
    }
}
