package fr.insa.esb.bus;

import fr.insa.esb.core.Message;

@FunctionalInterface
public interface MessageConsumer {
    void accept(Message message) throws Exception;
}