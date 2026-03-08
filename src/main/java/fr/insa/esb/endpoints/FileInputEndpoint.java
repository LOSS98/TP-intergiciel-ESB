package fr.insa.esb.endpoints;

import fr.insa.esb.bus.MessageBus;
import fr.insa.esb.core.EndpointType;
import fr.insa.esb.core.Message;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileInputEndpoint extends AbstractEndpoint {
    
    private final Path directoryPath;
    private final Set<String> allowedExtensions;
    private final boolean deleteAfterProcessing;
    private WatchService watchService;
    private ExecutorService executorService;
    
    public FileInputEndpoint(String name, MessageBus messageBus, String topic, 
                           String directory, Set<String> allowedExtensions, 
                           boolean deleteAfterProcessing) {
        super(name, messageBus, topic);
        this.directoryPath = Paths.get(directory);
        this.allowedExtensions = allowedExtensions;
        this.deleteAfterProcessing = deleteAfterProcessing;
    }
    
    public FileInputEndpoint(String name, MessageBus messageBus, String topic, 
                           String directory, Set<String> allowedExtensions) {
        this(name, messageBus, topic, directory, allowedExtensions, false);
    }
    
    @Override
    public void start() {
        if (running) {
            return;
        }
        
        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
                logger.info("Created directory: {}", directoryPath);
            }
            
            watchService = FileSystems.getDefault().newWatchService();
            directoryPath.register(watchService, 
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY);
            
            executorService = Executors.newSingleThreadExecutor();
            running = true;
            
            processExistingFiles();
            
            executorService.submit(this::watchForChanges);
            
            logger.info("FileInputEndpoint {} started, watching directory: {}", name, directoryPath);
            
        } catch (IOException e) {
            logger.error("Failed to start FileInputEndpoint {}", name, e);
            throw new RuntimeException("Failed to start endpoint", e);
        }
    }
    
    @Override
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        
        if (executorService != null) {
            executorService.shutdown();
        }
        
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                logger.error("Error closing watch service", e);
            }
        }
        
        logger.info("FileInputEndpoint {} stopped", name);
    }
    
    @Override
    public EndpointType getType() {
        return EndpointType.FILE_IN;
    }
    
    private void processExistingFiles() {
        try {
            Files.list(directoryPath)
                .filter(Files::isRegularFile)
                .forEach(this::processFile);
        } catch (IOException e) {
            logger.error("Error processing existing files in {}", directoryPath, e);
        }
    }
    
    private void watchForChanges() {
        while (running) {
            try {
                WatchKey key = watchService.take();
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();
                    Path filePath = directoryPath.resolve(fileName);
                    
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE || 
                        kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        
                        Thread.sleep(100);
                        processFile(filePath);
                    }
                }
                
                if (!key.reset()) {
                    break;
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error in watch service", e);
            }
        }
    }
    
    private void processFile(Path filePath) {
        if (!Files.isRegularFile(filePath)) {
            return;
        }
        
        String fileName = filePath.getFileName().toString();
        
        if (!isAllowedExtension(fileName)) {
            logger.debug("File {} has not allowed extension, skipping", fileName);
            return;
        }
        
        try {
            String content = Files.readString(filePath);
            
            Message message = new Message(content);
            message.addHeader("fileName", fileName);
            message.addHeader("filePath", filePath.toString());
            message.addHeader("fileSize", String.valueOf(Files.size(filePath)));
            message.setSourceEndpoint(name);
            
            generateMessageId(message);
            
            if (applyFilters(message)) {
                Message transformedMessage = applyTransformations(message);
                messageBus.publish(topic, transformedMessage);
                
                logger.info("File {} processed and published to topic {}", fileName, topic);
                
                if (deleteAfterProcessing) {
                    Files.deleteIfExists(filePath);
                    logger.debug("File {} deleted after processing", fileName);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error processing file {}", fileName, e);
        }
    }
    
    private boolean isAllowedExtension(String fileName) {
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            return true;
        }
        
        return allowedExtensions.stream()
            .anyMatch(ext -> fileName.toLowerCase().endsWith(ext.toLowerCase()));
    }
}