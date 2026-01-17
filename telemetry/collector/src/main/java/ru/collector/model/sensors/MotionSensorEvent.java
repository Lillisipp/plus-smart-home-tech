package ru.collector.model.sensors;

import lombok.Getter;
import lombok.Setter;
import ru.collector.model.SensorEvent;
import ru.collector.model.enams.SensorEventType;

@Setter
@Getter
public class MotionSensorEvent extends SensorEvent {
    private boolean motion;
    private int linkQuality;
    private int voltage;

    @Override
    public SensorEventType getEventType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}
