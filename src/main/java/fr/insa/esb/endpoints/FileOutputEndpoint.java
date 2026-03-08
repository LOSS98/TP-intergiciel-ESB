package fr.insa.esb.endpoints;

import fr.insa.esb.bus.MessageBus;
import fr.insa.esb.core.EndpointType;
import fr.insa.esb.core.Message;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileOutputEndpoint extends AbstractEndpoint {
    
    private final Path outputDirectory;
    private final boolean keepOriginalName;
    private final String filePrefix;
    private final String fileExtension;
    
    public FileOutputEndpoint(String name, MessageBus messageBus, String topic, 
                            String outputDirectory, boolean keepOriginalName, 
                            String filePrefix, String fileExtension) {
        super(name, messageBus, topic);
        this.outputDirectory = Paths.get(outputDirectory);
        this.keepOriginalName = keepOriginalName;
        this.filePrefix = filePrefix != null ? filePrefix : "";
        this.fileExtension = fileExtension != null ? fileExtension : ".txt";
    }
    
    public FileOutputEndpoint(String name, MessageBus messageBus, String topic, 
                            String outputDirectory, boolean keepOriginalName) {
        this(name, messageBus, topic, outputDirectory, keepOriginalName, "", ".txt");
    }
    
    public FileOutputEndpoint(String name, MessageBus messageBus, String topic, 
                            String outputDirectory) {
        this(name, messageBus, topic, outputDirectory, true, "", ".txt");
    }
    
    @Override
    public void start() {
        if (running) {
            return;
        }
        
        try {
            if (!Files.exists(outputDirectory)) {
                Files.createDirectories(outputDirectory);
                logger.info("Created output directory: {}", outputDirectory);
            }
            
            messageBus.subscribe(topic, this::processMessage);
            running = true;
            
            logger.info("FileOutputEndpoint {} started, writing to directory: {}", name, outputDirectory);
            
        } catch (Exception e) {
            logger.error("Failed to start FileOutputEndpoint {}", name, e);
            throw new RuntimeException("Failed to start endpoint", e);
        }
    }
    
    @Override
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        logger.info("FileOutputEndpoint {} stopped", name);
    }
    
    @Override
    public EndpointType getType() {
        return EndpointType.FILE_OUT;
    }
    
    private void processMessage(Message message) {
        try {
            if (!applyFilters(message)) {
                return;
            }
            
            Message transformedMessage = applyTransformations(message);
            
            String fileName = generateFileName(transformedMessage);
            Path filePath = outputDirectory.resolve(fileName);
            
            Files.writeString(filePath, transformedMessage.getContent(), 
                            StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            
            logger.info("Message {} written to file: {}", transformedMessage.getId(), fileName);
            
        } catch (Exception e) {
            logger.error("Error processing message {} in FileOutputEndpoint {}", 
                        message.getId(), name, e);
        }
    }
    
    private String generateFileName(Message message) {
        if (keepOriginalName) {
            String originalFileName = message.getHeader("fileName");
            if (originalFileName != null && !originalFileName.isEmpty()) {
                return filePrefix + originalFileName;
            }
        }
        
        String baseName = filePrefix + message.getId();
        
        if (!fileExtension.startsWith(".")) {
            baseName += ".";
        }
        baseName += fileExtension;
        
        return baseName;
    }
}