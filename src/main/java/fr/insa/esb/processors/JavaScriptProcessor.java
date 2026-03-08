package fr.insa.esb.processors;

import fr.insa.esb.core.Message;
import fr.insa.esb.core.MessageFilter;
import fr.insa.esb.core.MessageTransformer;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.Invocable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class JavaScriptProcessor implements MessageFilter, MessageTransformer {
    private static final Logger logger = LoggerFactory.getLogger(JavaScriptProcessor.class);
    
    private final String script;
    private final String functionName;
    private final ScriptEngine engine;
    
    public JavaScriptProcessor(String scriptContent, String functionName) {
        this.script = scriptContent;
        this.functionName = functionName;
        
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        this.engine = factory.getScriptEngine();
        
        try {
            engine.eval(script);
        } catch (ScriptException e) {
            logger.error("Error initializing JavaScript processor", e);
            throw new RuntimeException("Failed to initialize JavaScript processor", e);
        }
    }
    
    public static JavaScriptProcessor fromFile(String scriptPath, String functionName) {
        try {
            Path path = Paths.get(scriptPath);
            String scriptContent = Files.readString(path);
            return new JavaScriptProcessor(scriptContent, functionName);
        } catch (Exception e) {
            logger.error("Error loading JavaScript from file: {}", scriptPath, e);
            throw new RuntimeException("Failed to load JavaScript file", e);
        }
    }
    
    @Override
    public boolean filter(Message message) throws Exception {
        try {
            Invocable invocable = (Invocable) engine;
            Object messageObject = createMessageObject(message);
            
            Object result = invocable.invokeFunction(functionName, messageObject);
            
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
            return Boolean.parseBoolean(result.toString());
            
        } catch (Exception e) {
            logger.error("Error executing filter function '{}' for message {}", 
                        functionName, message.getId(), e);
            throw e;
        }
    }
    
    @Override
    public Message transform(Message message) throws Exception {
        try {
            Invocable invocable = (Invocable) engine;
            Object messageObject = createMessageObject(message);
            
            Object result = invocable.invokeFunction(functionName, messageObject);
            
            return extractMessageFromResult(result, message);
            
        } catch (Exception e) {
            logger.error("Error executing transform function '{}' for message {}", 
                        functionName, message.getId(), e);
            throw e;
        }
    }
    
    private Object createMessageObject(Message message) {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("id", message.getId());
        messageMap.put("content", message.getContent());
        messageMap.put("headers", message.getHeaders() != null ? message.getHeaders() : new HashMap<>());
        messageMap.put("sourceEndpoint", message.getSourceEndpoint());
        messageMap.put("timestamp", message.getTimestamp().toString());
        
        return messageMap;
    }
    
    @SuppressWarnings("unchecked")
    private Message extractMessageFromResult(Object result, Message originalMessage) {
        Message newMessage = new Message();
        
        if (result instanceof Map) {
            Map<String, Object> resultMap = (Map<String, Object>) result;
            
            newMessage.setId(resultMap.containsKey("id") && resultMap.get("id") != null ? 
                resultMap.get("id").toString() : originalMessage.getId());
            
            newMessage.setContent(resultMap.containsKey("content") && resultMap.get("content") != null ? 
                resultMap.get("content").toString() : originalMessage.getContent());
            
            if (resultMap.containsKey("headers") && resultMap.get("headers") instanceof Map) {
                Map<String, Object> headers = (Map<String, Object>) resultMap.get("headers");
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        newMessage.addHeader(entry.getKey(), entry.getValue().toString());
                    }
                }
            } else {
                newMessage.setHeaders(originalMessage.getHeaders());
            }
            
            newMessage.setSourceEndpoint(resultMap.containsKey("sourceEndpoint") && resultMap.get("sourceEndpoint") != null ? 
                resultMap.get("sourceEndpoint").toString() : originalMessage.getSourceEndpoint());
            
            newMessage.setDestinationEndpoint(resultMap.containsKey("destinationEndpoint") && resultMap.get("destinationEndpoint") != null ? 
                resultMap.get("destinationEndpoint").toString() : originalMessage.getDestinationEndpoint());
            
            newMessage.setTimestamp(originalMessage.getTimestamp());
            
        } else {
            newMessage.setContent(result.toString());
            newMessage.setId(originalMessage.getId());
            newMessage.setHeaders(originalMessage.getHeaders());
            newMessage.setSourceEndpoint(originalMessage.getSourceEndpoint());
            newMessage.setDestinationEndpoint(originalMessage.getDestinationEndpoint());
            newMessage.setTimestamp(originalMessage.getTimestamp());
        }
        
        return newMessage;
    }
    
    private String headersToJson(Message message) {
        if (message.getHeaders() == null || message.getHeaders().isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (var entry : message.getHeaders().entrySet()) {
            if (!first) {
                json.append(", ");
            }
            json.append("'").append(escapeString(entry.getKey())).append("': '")
                .append(escapeString(entry.getValue())).append("'");
            first = false;
        }
        json.append("}");
        return json.toString();
    }
    
    private String escapeString(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                   .replace("'", "\\'")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    public void close() {
        // Nashorn engine doesn't require explicit closing
    }
}