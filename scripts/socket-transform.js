function transform(message) {
    var timestamp = new Date().toISOString();
    var clientAddress = message.headers.clientAddress || "unknown";
    
    var transformedContent = "SOCKET_DATA|" + timestamp + "|" + clientAddress + "|" + message.content;
    
    // Copie manuelle des headers (compatible Nashorn ES5)
    var newHeaders = {};
    if (message.headers) {
        for (var key in message.headers) {
            newHeaders[key] = message.headers[key];
        }
    }
    newHeaders.processed = "true";
    newHeaders.processedAt = timestamp;
    newHeaders.transformer = "socket-transformer";
    newHeaders.originalClientAddress = clientAddress;
    
    return {
        id: message.id,
        content: transformedContent,
        headers: newHeaders,
        sourceEndpoint: message.sourceEndpoint,
        destinationEndpoint: message.destinationEndpoint
    };
}