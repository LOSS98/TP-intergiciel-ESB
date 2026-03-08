package fr.insa.esb.bus;

import com.rabbitmq.client.*;
import fr.insa.esb.core.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class RabbitMQMessageBus implements MessageBus {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQMessageBus.class);
    
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private Connection connection;
    private Channel channel;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, String> consumerTags;
    private volatile boolean running = false;
    
    public RabbitMQMessageBus(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.consumerTags = new ConcurrentHashMap<>();
    }
    
    public RabbitMQMessageBus(String host, int port) {
        this(host, port, "guest", "guest");
    }
    
    @Override
    public void start() throws Exception {
        if (running) {
            return;
        }
        
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setAutomaticRecoveryEnabled(true);
            
            connection = factory.newConnection();
            channel = connection.createChannel();
            running = true;
            
            logger.info("RabbitMQ connection established to {}:{}", host, port);
        } catch (IOException | TimeoutException e) {
            throw new Exception("Failed to connect to RabbitMQ", e);
        }
    }
    
    @Override
    public void stop() throws Exception {
        if (!running) {
            return;
        }
        
        running = false;
        
        consumerTags.values().forEach(consumerTag -> {
            try {
                channel.basicCancel(consumerTag);
            } catch (IOException e) {
                logger.error("Error cancelling consumer", e);
            }
        });
        consumerTags.clear();
        
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
            logger.info("RabbitMQ connection closed");
        } catch (IOException | TimeoutException e) {
            throw new Exception("Failed to close RabbitMQ connection", e);
        }
    }
    
    @Override
    public boolean isRunning() {
        return running && connection != null && connection.isOpen();
    }
    
    @Override
    public void publish(String topic, Message message) throws Exception {
        if (!running) {
            throw new IllegalStateException("Message bus is not running");
        }
        
        try {
            channel.queueDeclare(topic, true, false, false, null);
            String messageJson = objectMapper.writeValueAsString(message);
            channel.basicPublish("", topic, MessageProperties.PERSISTENT_TEXT_PLAIN, messageJson.getBytes());
            logger.debug("Message published to topic {}: {}", topic, message.getId());
        } catch (IOException e) {
            throw new Exception("Failed to publish message to topic: " + topic, e);
        }
    }
    
    @Override
    public void subscribe(String topic, MessageConsumer messageConsumer) throws Exception {
        if (!running) {
            throw new IllegalStateException("Message bus is not running");
        }
        
        try {
            channel.queueDeclare(topic, true, false, false, null);
            
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                         AMQP.BasicProperties properties, byte[] body) throws IOException {
                    try {
                        String messageJson = new String(body, "UTF-8");
                        Message message = objectMapper.readValue(messageJson, Message.class);
                        messageConsumer.accept(message);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                        logger.debug("Message consumed from topic {}: {}", topic, message.getId());
                    } catch (Exception e) {
                        logger.error("Error processing message from topic {}", topic, e);
                        try {
                            channel.basicNack(envelope.getDeliveryTag(), false, false);
                        } catch (IOException ex) {
                            logger.error("Failed to nack message", ex);
                        }
                    }
                }
            };
            
            String consumerTag = channel.basicConsume(topic, false, consumer);
            consumerTags.put(topic, consumerTag);
            logger.info("Subscribed to topic {} with consumer tag {}", topic, consumerTag);
            
        } catch (IOException e) {
            throw new Exception("Failed to subscribe to topic: " + topic, e);
        }
    }
}