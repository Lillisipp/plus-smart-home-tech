package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"scenario", "sensor", "condition"})
@Entity
@Table(name = "scenario_conditions")
public class ScenarioCondition {
    @EmbeddedId
    private ScenarioConditionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scenarioId")
    private Scenario scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sensorId")
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL)
    @MapsId("conditionId")
    private Condition condition;
}