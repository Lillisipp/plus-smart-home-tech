package ru.yandex.practicum.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ScenarioActionId implements Serializable {
    private Long scenarioId;
    private String sensorId;
    private Long actionId;
}