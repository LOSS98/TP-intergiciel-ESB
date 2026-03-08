package fr.insa.esb.endpoints;

import fr.insa.esb.bus.MessageBus;
import fr.insa.esb.core.EndpointType;
import fr.insa.esb.core.Message;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class SocketOutputEndpoint extends AbstractEndpoint {
    
    private final String host;
    private final int port;
    private final String protocol;
    private final boolean keepConnectionAlive;
    private Socket socket;
    private PrintWriter writer;
    
    public SocketOutputEndpoint(String name, MessageBus messageBus, String topic,
                              String host, int port, String protocol, boolean keepConnectionAlive) {
        super(name, messageBus, topic);
        this.host = host;
        this.port = port;
        this.protocol = protocol != null ? protocol : "TCP";
        this.keepConnectionAlive = keepConnectionAlive;
    }
    
    public SocketOutputEndpoint(String name, MessageBus messageBus, String topic,
                              String host, int port) {
        this(name, messageBus, topic, host, port, "TCP", false);
    }
    
    @Override
    public void start() {
        if (running) {
            return;
        }
        
        try {
            if (keepConnectionAlive) {
                establishConnection();
            }
            
            messageBus.subscribe(topic, this::processMessage);
            running = true;
            
            logger.info("SocketOutputEndpoint {} started, target: {}:{} ({})", 
                       name, host, port, protocol);
            
        } catch (Exception e) {
            logger.error("Failed to start SocketOutputEndpoint {}", name, e);
            throw new RuntimeException("Failed to start endpoint", e);
        }
    }
    
    @Override
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        closeConnection();
        
        logger.info("SocketOutputEndpoint {} stopped", name);
    }
    
    @Override
    public EndpointType getType() {
        return EndpointType.SOCKET_OUT;
    }
    
    private void processMessage(Message message) {
        try {
            if (!applyFilters(message)) {
                return;
            }
            
            Message transformedMessage = applyTransformations(message);
            sendMessage(transformedMessage);
            
        } catch (Exception e) {
            logger.error("Error processing message {} in SocketOutputEndpoint {}", 
                        message.getId(), name, e);
        }
    }
    
    private void sendMessage(Message message) throws IOException {
        if (!keepConnectionAlive) {
            try (Socket tempSocket = new Socket(host, port);
                 PrintWriter tempWriter = new PrintWriter(tempSocket.getOutputStream(), true)) {
                
                tempWriter.println(message.getContent());
                logger.info("Message {} sent to {}:{}", message.getId(), host, port);
            }
        } else {
            if (!isConnectionValid()) {
                establishConnection();
            }
            
            try {
                writer.println(message.getContent());
                writer.flush();
                logger.info("Message {} sent to {}:{}", message.getId(), host, port);
                
            } catch (Exception e) {
                logger.warn("Connection error, attempting to reconnect...");
                closeConnection();
                establishConnection();
                writer.println(message.getContent());
                writer.flush();
                logger.info("Message {} sent to {}:{} (after reconnect)", message.getId(), host, port);
            }
        }
    }
    
    private void establishConnection() throws IOException {
        try {
            socket = new Socket(host, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            logger.debug("Connection established to {}:{}", host, port);
            
        } catch (IOException e) {
            logger.error("Failed to establish connection to {}:{}", host, port, e);
            throw e;
        }
    }
    
    private void closeConnection() {
        if (writer != null) {
            writer.close();
            writer = null;
        }
        
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                logger.debug("Connection closed to {}:{}", host, port);
            } catch (IOException e) {
                logger.error("Error closing socket connection", e);
            } finally {
                socket = null;
            }
        }
    }
    
    private boolean isConnectionValid() {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            return false;
        }
        
        try {
            socket.sendUrgentData(0xFF);
            return true;
        } catch (SocketException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}