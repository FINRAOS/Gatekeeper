package org.finra.gatekeeper.services.db.connections.model;

import java.util.Map;

public class LambdaPayload {
    private Map<String, String> headers;
    private String httpMethod;
    private String path;
    private String body;
    private boolean base64Encoded;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public LambdaPayload setBody(String body) {
        this.body = body;
        return this;
    }

    public LambdaPayload setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public LambdaPayload setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public LambdaPayload setPath(String path) {
        this.path = path;
        return this;
    }

    public boolean isBase64Encoded() {
        return base64Encoded;
    }

    public LambdaPayload setBase64Encoded(boolean base64Encoded) {
        this.base64Encoded = base64Encoded;
        return this;
    }
}
