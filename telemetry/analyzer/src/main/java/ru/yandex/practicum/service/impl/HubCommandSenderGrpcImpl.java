package ru.yandex.practicum.service.impl;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.ScenarioAction;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.service.HubCommandSender;

import java.time.Instant;
import java.util.Set;


@Slf4j
@Service
public class HubCommandSenderGrpcImpl implements HubCommandSender {

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    @Override
    public void sendScenarioActions(String hubId, Scenario scenario, Set<ScenarioAction> actions) {
        log.info("ENTER sendScenarioActions: hubId={}, scenarioName={}, actionsCount={}",
                hubId, scenario.getName(), actions == null ? 0 : actions.size());

        if (actions == null || actions.isEmpty()) {
            log.info("EXIT sendScenarioActions: nothing to send. hubId={}, scenarioName={}", hubId, scenario.getName());
            return;
        }

        for (ScenarioAction link : actions) {
            Sensor sensor = link.getSensor();
            String sensorId = (sensor == null ? null : sensor.getId());

            if (sensorId == null) {
                log.warn("Skip action: sensorId is null. hubId={}, scenarioName={}", hubId, scenario.getName());
                continue;
            }

            Action action = link.getAction();
            if (action == null) {
                log.warn("Skip action: Action is null. hubId={}, scenarioName={}, sensorId={}",
                        hubId, scenario.getName(), sensorId);
                continue;
            }

            try {
                DeviceActionProto actionProto = DeviceActionProto.newBuilder()
                        .setSensorId(sensorId)
                        .setType(mapActionType(action))
                        .setValue(action.getValue() == null ? 0 : action.getValue())
                        .build();

                DeviceActionRequest request = DeviceActionRequest.newBuilder()
                        .setHubId(hubId)
                        .setScenarioName(scenario.getName())
                        .setAction(actionProto)
                        .setTimestamp(toProtoTimestamp(Instant.now()))
                        .build();

                log.info("gRPC send: hubId={}, scenarioName={}, sensorId={}, type={}, value={}",
                        hubId, scenario.getName(), sensorId, action.getType(), action.getValue());

                hubRouterClient.handleDeviceAction(request);

                log.info("gRPC sent: hubId={}, scenarioName={}, sensorId={}",
                        hubId, scenario.getName(), sensorId);

            } catch (Exception ex) {
                log.error("gRPC failed: hubId={}, scenarioName={}, sensorId={}",
                        hubId, scenario.getName(), sensorId, ex);
            }
        }

        log.info("EXIT sendScenarioActions: hubId={}, scenarioName={}", hubId, scenario.getName());
    }

    private ActionTypeProto mapActionType(Action action) {
        return ActionTypeProto.valueOf(action.getType().name());
    }

    private Timestamp toProtoTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}