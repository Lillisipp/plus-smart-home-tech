package ru.collector.model.sensors;

import lombok.Getter;
import lombok.Setter;
import ru.collector.model.SensorEvent;
import ru.collector.model.enams.SensorEventType;

@Setter
@Getter
public class ClimateSensorEvent extends SensorEvent {
    private Integer temperatureC;
    private Integer humidity;
    private Integer co2Level;

    @Override
    public SensorEventType getEventType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}
