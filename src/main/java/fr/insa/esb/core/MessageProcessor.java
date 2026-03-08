package fr.insa.esb.core;

public interface MessageProcessor {
    Message process(Message message) throws Exception;
}