package org.finra.gatekeeper.services.db.connections.model;

public class LambdaDTO {
    String dbEngine;
    LambdaQuery lambdaQuery;

    public String getDbEngine() {
        return dbEngine;
    }

    public LambdaDTO withDbEngine(String dbEngine) {
        this.dbEngine = dbEngine;
        return this;
    }

    public LambdaQuery getLambdaQuery() {
        return lambdaQuery;
    }

    public LambdaDTO withLambdaQuery(LambdaQuery lambdaQuery) {
        this.lambdaQuery = lambdaQuery;
        return this;
    }
}
