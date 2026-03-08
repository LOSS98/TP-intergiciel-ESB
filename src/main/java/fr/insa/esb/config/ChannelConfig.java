package fr.insa.esb.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ChannelConfig {
    
    @JsonProperty("channelName")
    private String channelName;
    
    @JsonProperty("topic")
    private String topic;
    
    @JsonProperty("source")
    private EndpointConfig source;
    
    @JsonProperty("destinations")
    private List<EndpointConfig> destinations;
    
    public String getChannelName() {
        return channelName;
    }
    
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public EndpointConfig getSource() {
        return source;
    }
    
    public void setSource(EndpointConfig source) {
        this.source = source;
    }
    
    public List<EndpointConfig> getDestinations() {
        return destinations;
    }
    
    public void setDestinations(List<EndpointConfig> destinations) {
        this.destinations = destinations;
    }
}