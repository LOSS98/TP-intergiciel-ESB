package fr.insa.mesh.analysis.config;

import fr.insa.mesh.grpc.SampleServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ManagedChannel sampleApiGrpcChannel(
            @Value("${sample.api.grpc.host:sample-api}") String host,
            @Value("${sample.api.grpc.port:9090}") int port) {
        return ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build();
    }

    @Bean
    public SampleServiceGrpc.SampleServiceBlockingStub sampleServiceStub(ManagedChannel sampleApiGrpcChannel) {
        return SampleServiceGrpc.newBlockingStub(sampleApiGrpcChannel);
    }
}
