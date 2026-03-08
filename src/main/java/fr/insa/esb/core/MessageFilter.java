package fr.insa.esb.core;

public interface MessageFilter {
    boolean filter(Message message) throws Exception;
}