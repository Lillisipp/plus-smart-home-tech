package ru.collector.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.collector.model.enams.ActionType;

@Getter
@Setter
public class DeviceAction {
    @NotBlank
    private String sensorId; // sensor_id

    @NotNull
    private ActionType type;

    private Integer value;
}
