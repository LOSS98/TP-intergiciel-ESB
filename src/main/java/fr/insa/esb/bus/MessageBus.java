package fr.insa.esb.bus;

import fr.insa.esb.core.Message;

public interface MessageBus {
    void publish(String topic, Message message) throws Exception;
    void subscribe(String topic, MessageConsumer consumer) throws Exception;
    void start() throws Exception;
    void stop() throws Exception;
    boolean isRunning();
}