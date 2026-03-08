package fr.insa.esb.endpoints;

import fr.insa.esb.bus.MessageBus;
import fr.insa.esb.core.EndpointType;
import fr.insa.esb.core.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketInputEndpoint extends AbstractEndpoint {
    
    private final String host;
    private final int port;
    private final String protocol;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    
    public SocketInputEndpoint(String name, MessageBus messageBus, String topic, 
                             String host, int port, String protocol) {
        super(name, messageBus, topic);
        this.host = host;
        this.port = port;
        this.protocol = protocol != null ? protocol : "TCP";
    }
    
    public SocketInputEndpoint(String name, MessageBus messageBus, String topic, 
                             String host, int port) {
        this(name, messageBus, topic, host, port, "TCP");
    }
    
    @Override
    public void start() {
        if (running) {
            return;
        }
        
        try {
            serverSocket = new ServerSocket(port);
            executorService = Executors.newCachedThreadPool();
            running = true;
            
            executorService.submit(this::acceptConnections);
            
            logger.info("SocketInputEndpoint {} started, listening on {}:{} ({})", 
                       name, host, port, protocol);
            
        } catch (IOException e) {
            logger.error("Failed to start SocketInputEndpoint {} on port {}", name, port, e);
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
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Error closing server socket", e);
            }
        }
        
        logger.info("SocketInputEndpoint {} stopped", name);
    }
    
    @Override
    public EndpointType getType() {
        return EndpointType.SOCKET_IN;
    }
    
    private void acceptConnections() {
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClient(clientSocket));
                
            } catch (IOException e) {
                if (running) {
                    logger.error("Error accepting connection", e);
                }
            }
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()))) {
            
            String clientAddress = clientSocket.getRemoteSocketAddress().toString();
            logger.debug("Client connected from: {}", clientAddress);
            
            String line;
            StringBuilder messageContent = new StringBuilder();
            
            while ((line = reader.readLine()) != null && running) {
                messageContent.append(line).append("\n");
                
                if (line.isEmpty()) {
                    String content = messageContent.toString().trim();
                    if (!content.isEmpty()) {
                        processSocketData(content, clientAddress);
                    }
                    messageContent.setLength(0);
                }
            }
            
            if (messageContent.length() > 0) {
                String content = messageContent.toString().trim();
                if (!content.isEmpty()) {
                    processSocketData(content, clientAddress);
                }
            }
            
        } catch (IOException e) {
            logger.error("Error handling client connection", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.error("Error closing client socket", e);
            }
        }
    }
    
    private void processSocketData(String data, String clientAddress) {
        try {
            Message message = new Message(data);
            message.addHeader("clientAddress", clientAddress);
            message.addHeader("protocol", protocol);
            message.addHeader("serverPort", String.valueOf(port));
            message.setSourceEndpoint(name);
            
            generateMessageId(message);
            
            if (applyFilters(message)) {
                Message transformedMessage = applyTransformations(message);
                messageBus.publish(topic, transformedMessage);
                
                logger.info("Socket data from {} processed and published to topic {}", 
                           clientAddress, topic);
            }
            
        } catch (Exception e) {
            logger.error("Error processing socket data from {}", clientAddress, e);
        }
    }
}