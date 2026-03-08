package fr.insa.esb.endpoints;

import fr.insa.esb.bus.MessageBus;
import fr.insa.esb.core.Endpoint;
import fr.insa.esb.core.Message;
import fr.insa.esb.core.MessageFilter;
import fr.insa.esb.core.MessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractEndpoint implements Endpoint {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractEndpoint.class);
    
    protected final String name;
    protected final MessageBus messageBus;
    protected final String topic;
    protected final List<MessageFilter> filters;
    protected final List<MessageTransformer> transformers;
    protected volatile boolean running = false;
    
    protected AbstractEndpoint(String name, MessageBus messageBus, String topic) {
        this.name = name;
        this.messageBus = messageBus;
        this.topic = topic;
        this.filters = new ArrayList<>();
        this.transformers = new ArrayList<>();
    }
    
    public void addFilter(MessageFilter filter) {
        this.filters.add(filter);
    }
    
    public void addTransformer(MessageTransformer transformer) {
        this.transformers.add(transformer);
    }
    
    protected boolean applyFilters(Message message) {
        for (MessageFilter filter : filters) {
            try {
                if (!filter.filter(message)) {
                    logger.debug("Message {} filtered out by {}", message.getId(), filter.getClass().getSimpleName());
                    return false;
                }
            } catch (Exception e) {
                logger.error("Error applying filter", e);
                return false;
            }
        }
        return true;
    }
    
    protected Message applyTransformations(Message message) throws Exception {
        Message transformedMessage = message;
        for (MessageTransformer transformer : transformers) {
            transformedMessage = transformer.transform(transformedMessage);
        }
        return transformedMessage;
    }
    
    protected void generateMessageId(Message message) {
        if (message.getId() == null) {
            message.setId(UUID.randomUUID().toString());
        }
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
}