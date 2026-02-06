package ru.yandex.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.model.enums.ActionType;
import ru.yandex.practicum.model.enums.ConditionOperation;
import ru.yandex.practicum.model.enums.ConditionType;
import ru.yandex.practicum.repository.*;
import ru.yandex.practicum.service.HubEventService;

@Service
@Slf4j
@RequiredArgsConstructor
public class HubEventServiceImpl implements HubEventService {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionLinkRepository scenarioConditionLinkRepository;
    private final ScenarioActionLinkRepository scenarioActionLinkRepository;

    @Transactional
    @Override
    public void handle(HubEventAvro event) {
        log.info("ENTER handle(HubEventAvro): hubId={}, ts={}, payloadType={}",
                event.getHubId(),
                event.getTimestamp(),
                event.getPayload() == null ? "null" : event.getPayload().getClass().getSimpleName()
        );
        Object payload = event.getPayload();
        switch (payload) {
            case DeviceAddedEventAvro e -> handleDeviceAdded(event.getHubId(), e);
            case DeviceRemovedEventAvro e -> handleDeviceRemoved(event.getHubId(), e);
            case ScenarioAddedEventAvro e -> handleScenarioAdded(event.getHubId(), e);
            case ScenarioRemovedEventAvro e -> handleScenarioRemoved(event.getHubId(), e);
            case null -> log.warn("HubEvent payload is null: hubId={}, ts={}", event.getHubId(), event.getTimestamp());
            default -> log.warn("Unknown payload type: {} for hubId={}",
                    payload.getClass().getName(), event.getHubId());
        }
        log.info("EXIT handle(HubEventAvro): hubId={}, ts={}", event.getHubId(), event.getTimestamp());

    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro e) {
        String sensorId = e.getId();
        if (sensorRepository.existsById(sensorId)) {
            log.info("DeviceAdded ignored: sensor already exists. hubId={} , sensorId={}", hubId, sensorId);
            return;
        }
        Sensor sensor = new Sensor();
        sensor.setId(sensorId);
        sensor.setHubId(hubId);
        sensorRepository.save(sensor);
        log.info("DeviceAdded saved: hubId={}, sensorId={}", hubId, sensorId);
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro e) {
        String sensorId = e.getId();
        log.info("DeviceRemoved: hubId={}, sensorId={}", hubId, sensorId);
        scenarioConditionLinkRepository.deleteBySensorId(sensorId);
        scenarioActionLinkRepository.deleteBySensorId(sensorId);

        sensorRepository.deleteById(sensorId);
        log.info("DeviceRemoved: hubId={}, sensorId={}", hubId, sensorId);
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro e) {
        String scenarioName = e.getName();
        log.info("ScenarioAdded: hubId={}, name={}, conditions={}, actions={}",
                hubId, scenarioName, e.getConditions().size(), e.getActions().size());

        Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, scenarioName)
                .orElseGet(() -> {
                    Scenario s = new Scenario();
                    s.setHubId(hubId);
                    s.setName(scenarioName);
                    return s;
                });
        scenario = scenarioRepository.save(scenario);
        scenarioConditionLinkRepository.deleteByScenarioId(scenario.getId());
        scenarioActionLinkRepository.deleteByScenarioId(scenario.getId());
        for (ScenarioConditionAvro c : e.getConditions()) {
            String sensorId = c.getSensorId();

            ensureSensorExists(hubId, sensorId);

            Condition condition = new Condition();
            condition.setType(ConditionType.valueOf(c.getType().name()));
            condition.setOperation(ConditionOperation.valueOf(c.getOperation().name()));
            condition.setValue(convertConditionValueToInt(c.getValue()));
            condition = conditionRepository.save(condition);

            scenarioConditionLinkRepository.saveLink(scenario.getId(), sensorId, condition.getId());
        }
        for (DeviceActionAvro a : e.getActions()) {
            String sensorId = a.getSensorId();

            ensureSensorExists(hubId, sensorId);

            Action action = new Action();
            action.setType(ActionType.valueOf(a.getType().name()));
            action.setValue(a.getValue() == null ? null : (Integer) a.getValue());
            action = actionRepository.save(action);

            scenarioActionLinkRepository.saveLink(scenario.getId(), sensorId, action.getId());
        }
        log.info("ScenarioAdded saved: hubId={}, scenarioId={}, name={}",
                hubId, scenario.getId(), scenarioName);

    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro e) {
        String scenarioName = e.getName();
        log.info("ScenarioRemoved: hubId={}, name={}", hubId, scenarioName);

        scenarioRepository.findByHubIdAndName(hubId, scenarioName).ifPresentOrElse(scenario -> {
            scenarioConditionLinkRepository.deleteByScenarioId(scenario.getId());
            scenarioActionLinkRepository.deleteByScenarioId(scenario.getId());
            scenarioRepository.deleteById(scenario.getId());
            log.info("ScenarioRemoved deleted: hubId={}, scenarioId={}, name={}", hubId, scenario.getId(), scenarioName);
        }, () -> log.info("ScenarioRemoved ignored: not found. hubId={}, name={}", hubId, scenarioName));
    }

    private void ensureSensorExists(String hubId, String sensorId) {
        if (sensorRepository.existsById(sensorId)) {
            return;
        }
        Sensor sensor = new Sensor();
        sensor.setId(sensorId);
        sensor.setHubId(hubId);
        sensorRepository.save(sensor);
        log.info("Sensor auto-created for scenario linking: hubId={}, sensorId={}", hubId, sensorId);

    }

    private Integer convertConditionValueToInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Boolean b) {
            return b ? 1 : 0;
        }
        throw new IllegalArgumentException("Unsupported condition value type: " + value.getClass());
    }
}


