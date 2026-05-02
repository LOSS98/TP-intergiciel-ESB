package fr.insa.mesh.sample.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GrpcServerComponent {

    private final SampleGrpcService sampleGrpcService;
    private Server server;

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    public GrpcServerComponent(SampleGrpcService sampleGrpcService) {
        this.sampleGrpcService = sampleGrpcService;
    }

    @PostConstruct
    public void start() throws IOException {
        server = ServerBuilder.forPort(grpcPort)
            .addService(sampleGrpcService)
            .build()
            .start();
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
