package fr.insa.mesh.analysis.grpc;

import fr.insa.mesh.grpc.GetSampleRequest;
import fr.insa.mesh.grpc.SampleResponse;
import fr.insa.mesh.grpc.SampleServiceGrpc;
import fr.insa.mesh.grpc.UpdateStatusRequest;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;

@Component
public class SampleGrpcClient {

    private final SampleServiceGrpc.SampleServiceBlockingStub stub;

    public SampleGrpcClient(SampleServiceGrpc.SampleServiceBlockingStub stub) {
        this.stub = stub;
    }

    public SampleResponse getSample(String sampleId) {
        try {
            return stub.getSample(GetSampleRequest.newBuilder()
                .setSampleId(sampleId)
                .build());
        } catch (StatusRuntimeException e) {
            throw new IllegalArgumentException("Sample not found: " + sampleId + " (" + e.getStatus() + ")");
        }
    }

    public void updateStatus(String sampleId, String status) {
        try {
            stub.updateStatus(UpdateStatusRequest.newBuilder()
                .setSampleId(sampleId)
                .setStatus(status)
                .build());
        } catch (StatusRuntimeException e) {
            // Non-bloquant : la mise à jour du statut ne doit pas faire échouer l'analyse
        }
    }
}
