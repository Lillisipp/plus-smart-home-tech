package ru.collector.model.sensors;

import lombok.Getter;
import lombok.Setter;
import ru.collector.model.SensorEvent;
import ru.collector.model.enams.SensorEventType;
@Setter
@Getter
public class TemperatureSensorEvent extends SensorEvent {
    private int temperatureC;
    private int temperatureF;

    @Override
    public SensorEventType getEventType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }
}
