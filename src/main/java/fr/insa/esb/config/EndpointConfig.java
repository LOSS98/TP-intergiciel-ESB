package fr.insa.esb.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class EndpointConfig {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("directory")
    private String directory;
    
    @JsonProperty("extensions")
    private List<String> extensions;
    
    @JsonProperty("host")
    private String host;
    
    @JsonProperty("port")
    private Integer port;
    
    @JsonProperty("protocol")
    private String protocol;
    
    @JsonProperty("filterScript")
    private String filterScript;
    
    @JsonProperty("transformScript")
    private String transformScript;
    
    @JsonProperty("keepOriginalName")
    private Boolean keepOriginalName;
    
    @JsonProperty("deleteAfterProcessing")
    private Boolean deleteAfterProcessing;
    
    @JsonProperty("keepConnectionAlive")
    private Boolean keepConnectionAlive;
    
    @JsonProperty("parameters")
    private Map<String, Object> parameters;
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDirectory() {
        return directory;
    }
    
    public void setDirectory(String directory) {
        this.directory = directory;
    }
    
    public List<String> getExtensions() {
        return extensions;
    }
    
    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String getFilterScript() {
        return filterScript;
    }
    
    public void setFilterScript(String filterScript) {
        this.filterScript = filterScript;
    }
    
    public String getTransformScript() {
        return transformScript;
    }
    
    public void setTransformScript(String transformScript) {
        this.transformScript = transformScript;
    }
    
    public Boolean getKeepOriginalName() {
        return keepOriginalName;
    }
    
    public void setKeepOriginalName(Boolean keepOriginalName) {
        this.keepOriginalName = keepOriginalName;
    }
    
    public Boolean getDeleteAfterProcessing() {
        return deleteAfterProcessing;
    }
    
    public void setDeleteAfterProcessing(Boolean deleteAfterProcessing) {
        this.deleteAfterProcessing = deleteAfterProcessing;
    }
    
    public Boolean getKeepConnectionAlive() {
        return keepConnectionAlive;
    }
    
    public void setKeepConnectionAlive(Boolean keepConnectionAlive) {
        this.keepConnectionAlive = keepConnectionAlive;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}