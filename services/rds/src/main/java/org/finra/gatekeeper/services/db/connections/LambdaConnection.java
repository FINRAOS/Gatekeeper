/*
 * Copyright 2018. Gatekeeper Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.finra.gatekeeper.services.db.connections;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.ServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.dbcp.BasicDataSource;
import org.finra.gatekeeper.configuration.GatekeeperProperties;
import org.finra.gatekeeper.exception.GatekeeperException;
import org.finra.gatekeeper.rds.exception.GKUnsupportedDBException;
import org.finra.gatekeeper.rds.interfaces.DBConnection;
import org.finra.gatekeeper.rds.interfaces.GKUserCredentialsProvider;
import org.finra.gatekeeper.rds.model.*;
import org.finra.gatekeeper.services.aws.AwsSessionService;
import org.finra.gatekeeper.services.db.connections.model.LambdaDTO;
import org.finra.gatekeeper.services.db.connections.model.LambdaPayload;
import org.finra.gatekeeper.services.db.connections.model.LambdaQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Interface for dealing with AWS Lambda.
 */
@Component
public class LambdaConnection  implements DBConnection{
    private final Logger logger = LoggerFactory.getLogger(LambdaConnection.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AwsSessionService awsSessionService;
    private final GKUserCredentialsProvider gkUserCredentialsProvider;
    private final String gkUserName;
    private final String lambdaFunctionName;

    @Autowired
    public LambdaConnection(AwsSessionService awsSessionService,
                            GatekeeperProperties gatekeeperProperties,
                            @Qualifier("credentialsProvider") GKUserCredentialsProvider gkUserCredentialsProvider){
        this.gkUserCredentialsProvider = gkUserCredentialsProvider;
        this.gkUserName = gatekeeperProperties.getDb().getGkUser();
        this.lambdaFunctionName = gatekeeperProperties.getLambda().getFunction();
        this.awsSessionService = awsSessionService;

    }

    public Map ping(){
        return invokeLambda("ping", "GET", "{}");
    }

    private Map invokeLambda(String uri, String method, String body){
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        LambdaPayload lambdaPayload = new LambdaPayload().setBody(body)
                .setHeaders(headers)
                .setHttpMethod(method.toUpperCase())
                .setBase64Encoded(false)
                .setPath("/api/rds/" + uri);
        try {
            InvokeRequest invokeRequest = new InvokeRequest().withFunctionName(lambdaFunctionName).withPayload(OBJECT_MAPPER.writeValueAsString(lambdaPayload));
            AWSLambda awsLambda = awsSessionService.getAwsLambda();

            InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
            String invokeResultString = new String( invokeResult.getPayload().array());
            Map lambdaResult = OBJECT_MAPPER.readValue(invokeResultString, Map.class);
            logger.info(lambdaResult.toString());
            String statusCode = lambdaResult.get("statusCode").toString();
            if(!statusCode.equals("200")){
                throw new GatekeeperException("Unable to reach lambda, Status Code: " + statusCode);
            }
            return lambdaResult;
        } catch (Exception e) {
            logger.error(e.toString());
            return Collections.singletonMap("Error", e);
        }
    }

    public boolean grantAccess(RdsGrantAccessQuery rdsGrantAccessQuery) throws Exception{
        LambdaDTO lambdaDTO = new LambdaDTO().withDbEngine(rdsGrantAccessQuery.getDbEngine()).withLambdaQuery(new LambdaQuery(rdsGrantAccessQuery)).withGatekeeperPassword(gkUserCredentialsProvider.getGatekeeperSecret(rdsGrantAccessQuery));
        try {
            String body = OBJECT_MAPPER.writeValueAsString(lambdaDTO);
            String lambdaJson = invokeLambda("grantAccess", "POST", body).get("body").toString();
            boolean lambdaResult = OBJECT_MAPPER.readValue(lambdaJson, boolean.class);
            return lambdaResult;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean revokeAccess(RdsRevokeAccessQuery rdsRevokeAccessQuery) throws Exception{
        LambdaDTO lambdaDTO = new LambdaDTO().withDbEngine(rdsRevokeAccessQuery.getDbEngine()).withLambdaQuery(new LambdaQuery(rdsRevokeAccessQuery)).withGatekeeperPassword(gkUserCredentialsProvider.getGatekeeperSecret(rdsRevokeAccessQuery));
        try {
            String body = OBJECT_MAPPER.writeValueAsString(lambdaDTO);
            String lambdaJson = invokeLambda("revokeAccess", "POST", body).get("body").toString();
            boolean lambdaResult = OBJECT_MAPPER.readValue(lambdaJson, boolean.class);
            return lambdaResult;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> checkDb(RdsQuery rdsQuery) throws GKUnsupportedDBException{
        LambdaDTO lambdaDTO = new LambdaDTO().withDbEngine(rdsQuery.getDbEngine()).withLambdaQuery(new LambdaQuery(rdsQuery)).withGatekeeperPassword(gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery));
        try {
            String body = OBJECT_MAPPER.writeValueAsString(lambdaDTO);
            String lambdaJson = invokeLambda("checkDb", "POST", body).get("body").toString();
            List<String> lambdaResult = Arrays.asList(OBJECT_MAPPER.readValue(lambdaJson, String[].class));
            return lambdaResult;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Arrays.asList(e.toString());
        }
    }

    public List<DbUser> getUsers(RdsQuery rdsQuery) throws SQLException {
        LambdaDTO lambdaDTO = new LambdaDTO().withDbEngine(rdsQuery.getDbEngine()).withLambdaQuery(new LambdaQuery(rdsQuery)).withGatekeeperPassword(gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery));
        try {
            String body = OBJECT_MAPPER.writeValueAsString(lambdaDTO);
            String lambdaJson = invokeLambda("getUsers", "POST", body).get("body").toString();
            List<DbUser> lambdaResult = OBJECT_MAPPER.readValue(lambdaJson, new TypeReference<List<DbUser>>(){});
            return lambdaResult;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    public List<String> checkIfUsersHasTables(RdsCheckUsersTableQuery rdsQuery){
        return Collections.emptyList();
    }

    public List<String> getAvailableRoles(RdsQuery rdsQuery) throws SQLException{
        return Arrays.asList("gk_readonly", "gk_datafix", "gk_dba");
    }

    private String getGkUserName(String user, RoleType role){
        return role != null ? user + "_" + role.getShortSuffix() : user;
    }

    public Map<RoleType, List<String>> getAvailableTables(RdsQuery rdsQuery) throws SQLException{
        LambdaDTO lambdaDTO = new LambdaDTO().withDbEngine(rdsQuery.getDbEngine()).withLambdaQuery(new LambdaQuery(rdsQuery)).withGatekeeperPassword(gkUserCredentialsProvider.getGatekeeperSecret(rdsQuery));
        try {
            String body = OBJECT_MAPPER.writeValueAsString(lambdaDTO);
            String lambdaJson = invokeLambda("getAvailableSchemas", "POST", body).get("body").toString();
            Map lambdaResult = OBJECT_MAPPER.readValue(lambdaJson, new TypeReference<Map<RoleType, List<String>>>(){});
            return lambdaResult;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_MAP;
    }



}
