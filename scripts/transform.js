function transform(message) {
    var timestamp = new Date().toISOString();
    var transformedContent = "[" + timestamp + "] " + message.content;
    
    if (message.headers.fileName && message.headers.fileName.endsWith('.csv')) {
        transformedContent = transformedContent.replace(/,/g, '|');
    }
    
    // Copie manuelle des headers (compatible Nashorn ES5)
    var newHeaders = {};
    if (message.headers) {
        for (var key in message.headers) {
            newHeaders[key] = message.headers[key];
        }
    }
    newHeaders.processed = "true";
    newHeaders.processedAt = timestamp;
    newHeaders.transformer = "file-transformer";
    
    return {
        id: message.id,
        content: transformedContent,
        headers: newHeaders,
        sourceEndpoint: message.sourceEndpoint,
        destinationEndpoint: message.destinationEndpoint
    };
}