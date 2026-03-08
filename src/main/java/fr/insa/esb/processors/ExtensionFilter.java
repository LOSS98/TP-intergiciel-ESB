package fr.insa.esb.processors;

import fr.insa.esb.core.Message;
import fr.insa.esb.core.MessageFilter;

import java.util.Set;

public class ExtensionFilter implements MessageFilter {
    private final Set<String> allowedExtensions;
    
    public ExtensionFilter(Set<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }
    
    @Override
    public boolean filter(Message message) {
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            return true;
        }
        
        String fileName = message.getHeader("fileName");
        if (fileName == null) {
            return true;
        }
        
        return allowedExtensions.stream()
            .anyMatch(ext -> fileName.toLowerCase().endsWith(ext.toLowerCase()));
    }
}