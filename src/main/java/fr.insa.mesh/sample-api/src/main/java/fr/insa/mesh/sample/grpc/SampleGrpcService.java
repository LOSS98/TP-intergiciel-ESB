package fr.insa.mesh.sample.grpc;

import fr.insa.mesh.grpc.GetSampleRequest;
import fr.insa.mesh.grpc.SampleResponse;
import fr.insa.mesh.grpc.SampleServiceGrpc;
import fr.insa.mesh.grpc.UpdateStatusRequest;
import fr.insa.mesh.grpc.UpdateStatusResponse;
import fr.insa.mesh.sample.model.Sample;
import fr.insa.mesh.sample.service.SampleService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

@Component
public class SampleGrpcService extends SampleServiceGrpc.SampleServiceImplBase {

    private final SampleService sampleService;

    public SampleGrpcService(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @Override
    public void getSample(GetSampleRequest request, StreamObserver<SampleResponse> observer) {
        sampleService.findById(request.getSampleId()).ifPresentOrElse(
            sample -> {
                observer.onNext(SampleResponse.newBuilder()
                    .setId(sample.getId())
                    .setPatientName(sample.getPatientName())
                    .setExamType(sample.getExamType())
                    .setSampleType(sample.getSampleType())
                    .setStatus(sample.getStatus().name())
                    .build());
                observer.onCompleted();
            },
            () -> observer.onError(
                Status.NOT_FOUND.withDescription("Sample not found: " + request.getSampleId())
                    .asRuntimeException()
            )
        );
    }

    @Override
    public void updateStatus(UpdateStatusRequest request, StreamObserver<UpdateStatusResponse> observer) {
        try {
            Sample.Status newStatus = Sample.Status.valueOf(request.getStatus());
            sampleService.updateStatus(request.getSampleId(), newStatus).ifPresentOrElse(
                sample -> {
                    observer.onNext(UpdateStatusResponse.newBuilder()
                        .setSuccess(true)
                        .setNewStatus(sample.getStatus().name())
                        .build());
                    observer.onCompleted();
                },
                () -> observer.onError(
                    Status.NOT_FOUND.withDescription("Sample not found: " + request.getSampleId())
                        .asRuntimeException()
                )
            );
        } catch (IllegalArgumentException e) {
            observer.onError(
                Status.INVALID_ARGUMENT.withDescription("Invalid status: " + request.getStatus())
                    .asRuntimeException()
            );
        }
    }
}
