package fr.insa.esb.management;

import fr.insa.esb.bus.MessageBus;
import fr.insa.esb.bus.RabbitMQMessageBus;
import fr.insa.esb.config.ChannelConfig;
import fr.insa.esb.config.EndpointConfig;
import fr.insa.esb.core.Endpoint;
import fr.insa.esb.endpoints.FileInputEndpoint;
import fr.insa.esb.endpoints.FileOutputEndpoint;
import fr.insa.esb.endpoints.SocketInputEndpoint;
import fr.insa.esb.endpoints.SocketOutputEndpoint;
import fr.insa.esb.processors.JavaScriptProcessor;
import fr.insa.esb.processors.ExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChannelManager {
    private static final Logger logger = LoggerFactory.getLogger(ChannelManager.class);
    
    private final MessageBus messageBus;
    private final List<Endpoint> endpoints;
    private final ChannelConfig config;
    private volatile boolean running = false;
    
    public ChannelManager(ChannelConfig config, String rabbitHost, int rabbitPort) {
        this.config = config;
        this.messageBus = new RabbitMQMessageBus(rabbitHost, rabbitPort, "admin", "admin123");
        this.endpoints = new ArrayList<>();
    }
    
    public ChannelManager(ChannelConfig config) {
        this(config, "localhost", 5672);
    }
    
    public void start() throws Exception {
        if (running) {
            return;
        }
        
        logger.info("Starting channel: {}", config.getChannelName());
        
        messageBus.start();
        
        createSourceEndpoint();
        createDestinationEndpoints();
        
        for (Endpoint endpoint : endpoints) {
            endpoint.start();
        }
        
        running = true;
        logger.info("Channel {} started successfully", config.getChannelName());
    }
    
    public void stop() throws Exception {
        if (!running) {
            return;
        }
        
        logger.info("Stopping channel: {}", config.getChannelName());
        
        for (Endpoint endpoint : endpoints) {
            try {
                endpoint.stop();
            } catch (Exception e) {
                logger.error("Error stopping endpoint: {}", endpoint.getName(), e);
            }
        }
        
        endpoints.clear();
        
        messageBus.stop();
        
        running = false;
        logger.info("Channel {} stopped", config.getChannelName());
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public String getChannelName() {
        return config.getChannelName();
    }
    
    public List<Endpoint> getEndpoints() {
        return new ArrayList<>(endpoints);
    }
    
    private void createSourceEndpoint() {
        EndpointConfig sourceConfig = config.getSource();
        String endpointName = config.getChannelName() + "-source";
        
        Endpoint sourceEndpoint = createEndpoint(endpointName, sourceConfig, config.getTopic(), true);
        endpoints.add(sourceEndpoint);
    }
    
    private void createDestinationEndpoints() {
        for (int i = 0; i < config.getDestinations().size(); i++) {
            EndpointConfig destConfig = config.getDestinations().get(i);
            String endpointName = config.getChannelName() + "-dest-" + i;
            
            Endpoint destEndpoint = createEndpoint(endpointName, destConfig, config.getTopic(), false);
            endpoints.add(destEndpoint);
        }
    }
    
    private Endpoint createEndpoint(String name, EndpointConfig config, String topic, boolean isSource) {
        String type = config.getType().toLowerCase();
        
        Endpoint endpoint = switch (type) {
            case "file-in" -> createFileInputEndpoint(name, config, topic);
            case "file-out" -> createFileOutputEndpoint(name, config, topic);
            case "socket-in" -> createSocketInputEndpoint(name, config, topic);
            case "socket-out" -> createSocketOutputEndpoint(name, config, topic);
            default -> throw new IllegalArgumentException("Unknown endpoint type: " + type);
        };
        
        configureEndpointProcessing(endpoint, config);
        
        return endpoint;
    }
    
    private FileInputEndpoint createFileInputEndpoint(String name, EndpointConfig config, String topic) {
        Set<String> extensions = config.getExtensions() != null ? 
            new HashSet<>(config.getExtensions()) : new HashSet<>();
        
        boolean deleteAfterProcessing = config.getDeleteAfterProcessing() != null ? 
            config.getDeleteAfterProcessing() : false;
        
        return new FileInputEndpoint(name, messageBus, topic, config.getDirectory(), 
                                   extensions, deleteAfterProcessing);
    }
    
    private FileOutputEndpoint createFileOutputEndpoint(String name, EndpointConfig config, String topic) {
        boolean keepOriginalName = config.getKeepOriginalName() != null ? 
            config.getKeepOriginalName() : true;
        
        return new FileOutputEndpoint(name, messageBus, topic, config.getDirectory(), 
                                    keepOriginalName);
    }
    
    private SocketInputEndpoint createSocketInputEndpoint(String name, EndpointConfig config, String topic) {
        String host = config.getHost() != null ? config.getHost() : "localhost";
        String protocol = config.getProtocol() != null ? config.getProtocol() : "TCP";
        
        return new SocketInputEndpoint(name, messageBus, topic, host, config.getPort(), protocol);
    }
    
    private SocketOutputEndpoint createSocketOutputEndpoint(String name, EndpointConfig config, String topic) {
        String protocol = config.getProtocol() != null ? config.getProtocol() : "TCP";
        boolean keepConnectionAlive = config.getKeepConnectionAlive() != null ? 
            config.getKeepConnectionAlive() : false;
        
        return new SocketOutputEndpoint(name, messageBus, topic, config.getHost(), 
                                      config.getPort(), protocol, keepConnectionAlive);
    }
    
    private void configureEndpointProcessing(Endpoint endpoint, EndpointConfig config) {
        if (!(endpoint instanceof fr.insa.esb.endpoints.AbstractEndpoint)) {
            return;
        }
        
        fr.insa.esb.endpoints.AbstractEndpoint abstractEndpoint = 
            (fr.insa.esb.endpoints.AbstractEndpoint) endpoint;
        
        if (config.getExtensions() != null && !config.getExtensions().isEmpty()) {
            abstractEndpoint.addFilter(new ExtensionFilter(new HashSet<>(config.getExtensions())));
        }
        
        if (config.getFilterScript() != null && !config.getFilterScript().trim().isEmpty()) {
            try {
                JavaScriptProcessor filterProcessor = JavaScriptProcessor.fromFile(
                    config.getFilterScript(), "filter");
                abstractEndpoint.addFilter(filterProcessor);
            } catch (Exception e) {
                logger.error("Error loading filter script: {}", config.getFilterScript(), e);
            }
        }
        
        if (config.getTransformScript() != null && !config.getTransformScript().trim().isEmpty()) {
            try {
                JavaScriptProcessor transformProcessor = JavaScriptProcessor.fromFile(
                    config.getTransformScript(), "transform");
                abstractEndpoint.addTransformer(transformProcessor);
            } catch (Exception e) {
                logger.error("Error loading transform script: {}", config.getTransformScript(), e);
            }
        }
    }
}