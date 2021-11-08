package org.finra.gatekeeper.services.db.connections.model;

public class LambdaDTO {
    String gatekeeperPassword;
    String dbEngine;
    LambdaQuery lambdaQuery;

    public String getGatekeeperPassword() {
        return gatekeeperPassword;
    }

    public LambdaDTO withGatekeeperPassword(String gatekeeperPassword) {
        this.gatekeeperPassword = gatekeeperPassword;
        return this;
    }

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
