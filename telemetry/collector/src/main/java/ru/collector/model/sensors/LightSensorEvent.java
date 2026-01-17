package ru.collector.model.sensors;

import lombok.Getter;
import lombok.Setter;
import ru.collector.model.SensorEvent;
import ru.collector.model.enams.SensorEventType;

@Setter
@Getter
public class LightSensorEvent extends SensorEvent {
    private int luminosity;
    private int linkQuality;

    @Override
    public SensorEventType getEventType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }
}
