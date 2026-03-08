package fr.insa.esb;

import fr.insa.esb.config.ChannelConfig;
import fr.insa.esb.config.ConfigurationLoader;
import fr.insa.esb.core.Endpoint;
import fr.insa.esb.management.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ManagementService {
    private static final Logger logger = LoggerFactory.getLogger(ManagementService.class);
    
    private ChannelManager channelManager;
    private ScheduledExecutorService monitoringExecutor;
    private final ConfigurationLoader configLoader;
    
    public ManagementService() {
        this.configLoader = new ConfigurationLoader();
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar mini-esb.jar <config-file> [rabbitmq-host] [rabbitmq-port]");
            System.exit(1);
        }
        
        String configFile = args[0];
        String rabbitHost = args.length > 1 ? args[1] : "localhost";
        int rabbitPort = args.length > 2 ? Integer.parseInt(args[2]) : 5672;
        
        ManagementService service = new ManagementService();
        
        try {
            service.start(configFile, rabbitHost, rabbitPort);
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown signal received, stopping service...");
                service.stop();
            }));
            
            System.out.println("ESB Management Service started. Press Ctrl+C to stop.");
            
            Thread.currentThread().join();
            
        } catch (Exception e) {
            logger.error("Failed to start ESB Management Service", e);
            System.exit(1);
        }
    }
    
    public void start(String configFile, String rabbitHost, int rabbitPort) throws Exception {
        logger.info("Starting ESB Management Service...");
        logger.info("Configuration file: {}", configFile);
        logger.info("RabbitMQ: {}:{}", rabbitHost, rabbitPort);
        
        try {
            ChannelConfig config = configLoader.loadConfiguration(configFile);
            
            channelManager = new ChannelManager(config, rabbitHost, rabbitPort);
            channelManager.start();
            
            startMonitoring();
            
            logger.info("ESB Management Service started successfully");
            
        } catch (IOException e) {
            logger.error("Failed to load configuration from: {}", configFile, e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to start channel", e);
            throw e;
        }
    }
    
    public void stop() {
        logger.info("Stopping ESB Management Service...");
        
        if (monitoringExecutor != null && !monitoringExecutor.isShutdown()) {
            monitoringExecutor.shutdown();
            try {
                if (!monitoringExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    monitoringExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                monitoringExecutor.shutdownNow();
            }
        }
        
        if (channelManager != null) {
            try {
                channelManager.stop();
            } catch (Exception e) {
                logger.error("Error stopping channel manager", e);
            }
        }
        
        logger.info("ESB Management Service stopped");
    }
    
    private void startMonitoring() {
        monitoringExecutor = Executors.newScheduledThreadPool(1);
        
        monitoringExecutor.scheduleAtFixedRate(() -> {
            try {
                displayStatus();
            } catch (Exception e) {
                logger.error("Error in monitoring thread", e);
            }
        }, 10, 30, TimeUnit.SECONDS);
    }
    
    private void displayStatus() {
        if (channelManager == null) {
            return;
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ESB CHANNEL STATUS");
        System.out.println("=".repeat(60));
        System.out.printf("Channel Name: %s%n", channelManager.getChannelName());
        System.out.printf("Status: %s%n", channelManager.isRunning() ? "RUNNING" : "STOPPED");
        System.out.printf("Timestamp: %s%n", java.time.LocalDateTime.now());
        
        System.out.println("\nEndpoints:");
        System.out.println("-".repeat(40));
        
        for (Endpoint endpoint : channelManager.getEndpoints()) {
            String status = endpoint.isRunning() ? "RUNNING" : "STOPPED";
            System.out.printf("  %-20s %-12s %s%n", 
                            endpoint.getName(), 
                            endpoint.getType().name(), 
                            status);
        }
        
        System.out.println("=".repeat(60));
    }
    
    public ChannelManager getChannelManager() {
        return channelManager;
    }
    
    public boolean isRunning() {
        return channelManager != null && channelManager.isRunning();
    }
}