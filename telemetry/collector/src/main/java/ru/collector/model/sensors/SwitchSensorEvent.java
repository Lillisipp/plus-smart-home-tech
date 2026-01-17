package ru.collector.model.sensors;

import lombok.Getter;
import lombok.Setter;
import ru.collector.model.SensorEvent;
import ru.collector.model.enams.SensorEventType;

@Setter
@Getter
public class SwitchSensorEvent extends SensorEvent {
    private boolean state;

    @Override
    public SensorEventType getEventType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }
}
