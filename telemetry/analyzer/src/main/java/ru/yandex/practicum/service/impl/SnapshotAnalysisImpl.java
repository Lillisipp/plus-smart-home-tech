package ru.yandex.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.ScenarioAction;
import ru.yandex.practicum.model.ScenarioCondition;
import ru.yandex.practicum.model.enums.ConditionOperation;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.service.HubCommandSender;
import ru.yandex.practicum.service.SnapshotAnalysisService;
import ru.yandex.practicum.service.SnapshotValueReader;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotAnalysisImpl implements SnapshotAnalysisService {

    private final ScenarioRepository scenarioRepository;
    private final SnapshotValueReader snapshotValueReader;
    private final HubCommandSender hubCommandSender;

    @Transactional(readOnly = true)
    @Override
    public void handleSnapshot(SensorsSnapshotAvro snapshot) {
        // 0) защита от null
        if (snapshot == null) {
            log.warn("handleSnapshot: snapshot is null");
            return;
        }
        if (snapshot.getHubId() == null || snapshot.getHubId().isBlank()) {
            log.warn("handleSnapshot: hubId is null/blank, ts={}", snapshot.getTimestamp());
            return;
        }

        String hubId = snapshot.getHubId();
        log.info("ENTER handleSnapshot: hubId={}, ts={}", hubId, snapshot.getTimestamp());

        List<Scenario> scenarios;
        try {
            scenarios = scenarioRepository.findByHubId(hubId);
        } catch (Exception ex) {
            log.error("Cannot load scenarios from DB: hubId={}", hubId, ex);
            return;
        }

        if (scenarios == null || scenarios.isEmpty()) {
            log.info("No scenarios for hubId={}, wait next snapshot", hubId);
            log.info("EXIT handleSnapshot: hubId={}", hubId);
            return;
        }

        for (Scenario scenario : scenarios) {
            if (scenario == null) continue;

            try {
                boolean triggered = isTriggered(snapshot, scenario);

                log.info("Scenario checked: hubId={}, name={}, triggered={}",
                        hubId, scenario.getName(), triggered);

                if (!triggered) continue;

                Set<ScenarioAction> actions = scenario.getActions() == null ? Set.of() : scenario.getActions();
                if (actions.isEmpty()) {
                    log.info("Scenario triggered but actions empty: hubId={}, name={}", hubId, scenario.getName());
                    continue;
                }

                // 3) отправить команды
                hubCommandSender.sendScenarioActions(hubId, scenario, actions);

            } catch (Exception ex) {
                // не валим весь анализатор из-за одного сценария
                log.error("Scenario processing failed: hubId={}, scenarioId={}, name={}",
                        hubId, scenario.getId(), scenario.getName(), ex);
            }
        }

        log.info("EXIT handleSnapshot: hubId={}", hubId);
    }

    private boolean isTriggered(SensorsSnapshotAvro snapshot, Scenario scenario) {
        // 1) условия должны быть
        Set<ScenarioCondition> links = scenario.getConditions() == null ? Set.of() : scenario.getConditions();
        if (links.isEmpty()) {
            log.info("Scenario has no conditions -> not triggered. name={}", scenario.getName());
            return false;
        }

        for (ScenarioCondition link : links) {
            if (link == null) return false;

            Condition c = link.getCondition();
            if (c == null) {
                log.warn("ScenarioCondition without Condition: scenarioName={}", scenario.getName());
                return false;
            }
            // Вариант A (если есть связь Sensor sensor):
            String sensorId = (link.getSensor() == null ? null : link.getSensor().getId());

            // Вариант B (если у тебя есть поле sensorId):
            // String sensorId = link.getSensorId();

            if (sensorId == null || sensorId.isBlank()) {
                log.warn("Condition has no sensorId: scenarioName={}, conditionId={}",
                        scenario.getName(), c.getId());
                return false;
            }

            Integer actual = snapshotValueReader.readValue(snapshot, sensorId, c.getType());
            Integer target = c.getValue();

            log.info("Condition check: scenarioName={}, sensorId={}, type={}, op={}, target={}, actual={}",
                    scenario.getName(), sensorId, c.getType(), c.getOperation(), target, actual);

            if (!compare(actual, c.getOperation(), target)) {
                return false;
            }
        }

        return true;
    }

    private boolean compare(Integer actual, ConditionOperation op, Integer target) {
        if (actual == null || op == null || target == null) return false;

        return switch (op) {
            case EQUALS -> Objects.equals(actual, target);
            case GREATER_THAN -> actual > target;
            case LOWER_THAN -> actual < target;
        };
    }
}