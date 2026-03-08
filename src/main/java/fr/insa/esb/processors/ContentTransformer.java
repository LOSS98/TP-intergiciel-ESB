package fr.insa.esb.processors;

import fr.insa.esb.core.Message;
import fr.insa.esb.core.MessageTransformer;

import java.util.function.Function;

public class ContentTransformer implements MessageTransformer {
    private final Function<String, String> transformer;
    
    public ContentTransformer(Function<String, String> transformer) {
        this.transformer = transformer;
    }
    
    @Override
    public Message transform(Message message) {
        String transformedContent = transformer.apply(message.getContent());
        
        Message transformedMessage = new Message(transformedContent);
        transformedMessage.setId(message.getId());
        transformedMessage.setHeaders(message.getHeaders());
        transformedMessage.setSourceEndpoint(message.getSourceEndpoint());
        transformedMessage.setDestinationEndpoint(message.getDestinationEndpoint());
        transformedMessage.setTimestamp(message.getTimestamp());
        
        return transformedMessage;
    }
    
    public static ContentTransformer upperCase() {
        return new ContentTransformer(String::toUpperCase);
    }
    
    public static ContentTransformer lowerCase() {
        return new ContentTransformer(String::toLowerCase);
    }
    
    public static ContentTransformer addPrefix(String prefix) {
        return new ContentTransformer(content -> prefix + content);
    }
    
    public static ContentTransformer addSuffix(String suffix) {
        return new ContentTransformer(content -> content + suffix);
    }
    
    public static ContentTransformer replace(String target, String replacement) {
        return new ContentTransformer(content -> content.replace(target, replacement));
    }
}