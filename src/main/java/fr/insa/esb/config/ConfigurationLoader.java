package fr.insa.esb.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);
    
    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;
    
    public ConfigurationLoader() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.jsonMapper = new ObjectMapper();
    }
    
    public ChannelConfig loadConfiguration(String configPath) throws IOException {
        File configFile = new File(configPath);
        
        if (!configFile.exists()) {
            throw new IOException("Configuration file not found: " + configPath);
        }
        
        ObjectMapper mapper = getMapperForFile(configPath);
        
        try {
            ChannelConfig config = mapper.readValue(configFile, ChannelConfig.class);
            validateConfiguration(config);
            logger.info("Configuration loaded successfully from: {}", configPath);
            return config;
            
        } catch (Exception e) {
            logger.error("Error loading configuration from: {}", configPath, e);
            throw new IOException("Failed to load configuration", e);
        }
    }
    
    private ObjectMapper getMapperForFile(String configPath) {
        String lowerPath = configPath.toLowerCase();
        
        if (lowerPath.endsWith(".yml") || lowerPath.endsWith(".yaml")) {
            return yamlMapper;
        } else if (lowerPath.endsWith(".json")) {
            return jsonMapper;
        } else {
            logger.warn("Unknown file extension, trying YAML mapper first");
            return yamlMapper;
        }
    }
    
    private void validateConfiguration(ChannelConfig config) throws IllegalArgumentException {
        if (config.getChannelName() == null || config.getChannelName().trim().isEmpty()) {
            throw new IllegalArgumentException("Channel name is required");
        }
        
        if (config.getTopic() == null || config.getTopic().trim().isEmpty()) {
            throw new IllegalArgumentException("Topic is required");
        }
        
        if (config.getSource() == null) {
            throw new IllegalArgumentException("Source endpoint configuration is required");
        }
        
        validateEndpointConfig(config.getSource(), "source");
        
        if (config.getDestinations() == null || config.getDestinations().isEmpty()) {
            throw new IllegalArgumentException("At least one destination endpoint is required");
        }
        
        for (int i = 0; i < config.getDestinations().size(); i++) {
            validateEndpointConfig(config.getDestinations().get(i), "destination[" + i + "]");
        }
    }
    
    private void validateEndpointConfig(EndpointConfig config, String context) throws IllegalArgumentException {
        if (config.getType() == null || config.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Endpoint type is required for " + context);
        }
        
        String type = config.getType().toLowerCase();
        
        switch (type) {
            case "file-in":
            case "file-out":
                if (config.getDirectory() == null || config.getDirectory().trim().isEmpty()) {
                    throw new IllegalArgumentException("Directory is required for file endpoint " + context);
                }
                break;
                
            case "socket-in":
                if (config.getPort() == null || config.getPort() <= 0) {
                    throw new IllegalArgumentException("Valid port is required for socket input endpoint " + context);
                }
                break;
                
            case "socket-out":
                if (config.getHost() == null || config.getHost().trim().isEmpty()) {
                    throw new IllegalArgumentException("Host is required for socket output endpoint " + context);
                }
                if (config.getPort() == null || config.getPort() <= 0) {
                    throw new IllegalArgumentException("Valid port is required for socket output endpoint " + context);
                }
                break;
                
            default:
                throw new IllegalArgumentException("Unknown endpoint type: " + type + " for " + context);
        }
    }
}