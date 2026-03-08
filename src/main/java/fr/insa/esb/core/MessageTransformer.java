package fr.insa.esb.core;

public interface MessageTransformer {
    Message transform(Message message) throws Exception;
}