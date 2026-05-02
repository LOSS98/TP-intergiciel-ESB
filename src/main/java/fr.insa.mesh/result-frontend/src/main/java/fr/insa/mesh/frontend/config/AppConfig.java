package fr.insa.mesh.frontend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Value("${sample.api.url}")
    private String sampleApiUrl;

    @Value("${analysis.api.url}")
    private String analysisApiUrl;

    @Bean("sampleApiClient")
    public RestClient sampleApiClient() {
        return RestClient.builder().baseUrl(sampleApiUrl).build();
    }

    @Bean("analysisApiClient")
    public RestClient analysisApiClient() {
        return RestClient.builder().baseUrl(analysisApiUrl).build();
    }
}
