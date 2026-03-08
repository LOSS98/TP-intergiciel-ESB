package fr.insa.esb.core;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Message {
    private String id;
    private String content;
    private Map<String, String> headers;
    private LocalDateTime timestamp;
    private String sourceEndpoint;
    private String destinationEndpoint;
    
    public Message() {
        this.headers = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }
    
    public Message(String content) {
        this();
        this.content = content;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }
    
    public String getHeader(String key) {
        return this.headers.get(key);
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSourceEndpoint() {
        return sourceEndpoint;
    }
    
    public void setSourceEndpoint(String sourceEndpoint) {
        this.sourceEndpoint = sourceEndpoint;
    }
    
    public String getDestinationEndpoint() {
        return destinationEndpoint;
    }
    
    public void setDestinationEndpoint(String destinationEndpoint) {
        this.destinationEndpoint = destinationEndpoint;
    }
}