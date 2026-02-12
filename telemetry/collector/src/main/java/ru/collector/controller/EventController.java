package ru.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.collector.service.CollectorService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final CollectorService collectorService;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        log.info("collectSensorEvent: {}", request);
        try {
            SensorEventProto.PayloadCase payloadCase = request.getPayloadCase();
            collectorService.collectSensorEvent(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC collectSensorEvent failed: hubId={}, id={}, payloadCase={}",
                    request.getHubId(), request.getId(), request.getPayloadCase(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e).withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        log.info("collectHubEvent: {}", request);
        try {
            collectorService.collectHubEvent(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC collectHubEvent failed: hubId={}, payloadCase={}",
                    request.getHubId(), request.getPayloadCase(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e).withDescription(e.getMessage()).asRuntimeException());
        }
    }

}

