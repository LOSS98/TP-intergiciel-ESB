function filter(message) {
    if (!message.content || message.content.trim().length === 0) {
        return false;
    }
    
    var content = message.content.toLowerCase();
    if (content.indexOf("ping") !== -1) {
        return false;
    }
    
    return true;
}