package fr.insa.esb.core;

public interface Endpoint {
    void start();
    void stop();
    boolean isRunning();
    String getName();
    EndpointType getType();
}