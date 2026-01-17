package ru.collector.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.collector.model.enams.ConditionOperation;
import ru.collector.model.enams.ConditionType;

@Getter
@Setter
public class ScenarioCondition {

    @NotBlank
    private String sensorId;

    @NotNull
    private ConditionType type;

    @NotNull
    private ConditionOperation operation;

    @JsonIgnore
    private Integer intValue;

    @JsonIgnore
    private Boolean booleanValue;

    @JsonProperty("value")
    public void setValue(Object value) {
        this.intValue = null;
        this.booleanValue = null;

        if (value == null) {
            return;
        }
        if (value instanceof Integer i) {
            this.intValue = i;
            return;
        }
        if (value instanceof Boolean b) {
            this.booleanValue = b;
            return;
        }
        throw new IllegalArgumentException("ScenarioCondition.value must be integer or boolean, but was: " + value.getClass());
    }
}
