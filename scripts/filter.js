function filter(message) {
    if (!message.content || message.content.trim().length === 0) {
        return false;
    }
    
    var fileName = message.headers.fileName;
    if (fileName && typeof fileName === 'string' && fileName.toLowerCase().indexOf("temp") !== -1) {
        return false;
    }
    
    var fileSize = parseInt(message.headers.fileSize || "0");
    if (fileSize > 1024 * 1024) {
        return false;
    }
    
    return true;
}