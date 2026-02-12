package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"scenario", "sensor", "action"})
@Entity
@Table(name = "scenario_actions")
public class ScenarioAction {
    @EmbeddedId
    private ScenarioActionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scenarioId")
    private Scenario scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sensorId")
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL)
    @MapsId("actionId")
    private Action action;
}