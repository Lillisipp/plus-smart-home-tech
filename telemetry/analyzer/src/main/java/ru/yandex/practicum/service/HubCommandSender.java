package ru.yandex.practicum.service;

import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.ScenarioAction;

import java.util.Set;

public interface HubCommandSender {
    void sendScenarioActions(String hubId, Scenario scenario, Set<ScenarioAction> actions);

}
