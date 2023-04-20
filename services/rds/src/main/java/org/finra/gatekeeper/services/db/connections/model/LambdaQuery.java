package org.finra.gatekeeper.services.db.connections.model;

import org.finra.gatekeeper.rds.model.*;

import java.util.Collections;
import java.util.List;


public class LambdaQuery {
    //RDS Query Vars
    private String account;
    private String accountId;
    private String region;
    private String sdlc;
    private String address;
    private String dbInstanceName;
    private String dbEngine;
    //Rds Check Users Table
    private List<String> users = Collections.emptyList();
    //Rds Grant/Revoke Access
    private String user = "";
    private RoleType role = RoleType.READONLY;
    //Rds Grant Access
    private String password = "";
    private Integer time = 0;
    private boolean rdsIAMAuth;

    public LambdaQuery(RdsQuery rdsQuery){
        this.account = rdsQuery.getAccount();
        this.accountId = rdsQuery.getAccountId();
        this.region = rdsQuery.getRegion();
        this.sdlc = rdsQuery.getSdlc();
        this.address = rdsQuery.getAddress();
        this.dbInstanceName = rdsQuery.getDbInstanceName();
        this.dbEngine = rdsQuery.getDbEngine();
        this.rdsIAMAuth = rdsQuery.isRdsIAMAuth();
    }

    public LambdaQuery(RdsCheckUsersTableQuery rdsQuery){
        this.users = rdsQuery.getUsers();
        this.account = rdsQuery.getAccount();
        this.accountId = rdsQuery.getAccountId();
        this.region = rdsQuery.getRegion();
        this.sdlc = rdsQuery.getSdlc();
        this.address = rdsQuery.getAddress();
        this.dbInstanceName = rdsQuery.getDbInstanceName();
        this.dbEngine = rdsQuery.getDbEngine();
        this.rdsIAMAuth = rdsQuery.isRdsIAMAuth();
    }

    public LambdaQuery(RdsRevokeAccessQuery rdsQuery){
        this.user = rdsQuery.getUser();
        this.role = rdsQuery.getRole();
        this.account = rdsQuery.getAccount();
        this.accountId = rdsQuery.getAccountId();
        this.region = rdsQuery.getRegion();
        this.sdlc = rdsQuery.getSdlc();
        this.address = rdsQuery.getAddress();
        this.dbInstanceName = rdsQuery.getDbInstanceName();
        this.dbEngine = rdsQuery.getDbEngine();
        this.rdsIAMAuth = rdsQuery.isRdsIAMAuth();
    }

    public LambdaQuery(RdsGrantAccessQuery rdsQuery){
        this.user = rdsQuery.getUser();
        this.role = rdsQuery.getRole();
        this.password = rdsQuery.getPassword();
        this.time = rdsQuery.getTime();
        this.account = rdsQuery.getAccount();
        this.accountId = rdsQuery.getAccountId();
        this.region = rdsQuery.getRegion();
        this.sdlc = rdsQuery.getSdlc();
        this.address = rdsQuery.getAddress();
        this.dbInstanceName = rdsQuery.getDbInstanceName();
        this.dbEngine = rdsQuery.getDbEngine();
        this.rdsIAMAuth = rdsQuery.isRdsIAMAuth();
    }

    public String getAccount() {
        return account;
    }

    public LambdaQuery withAccount(String account) {
        this.account = account;
        return this;
    }

    public String getAccountId() {
        return accountId;
    }

    public LambdaQuery withAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public LambdaQuery withRegion(String region) {
        this.region = region;
        return this;
    }

    public String getSdlc() {
        return sdlc;
    }

    public LambdaQuery withSdlc(String sdlc) {
        this.sdlc = sdlc;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public LambdaQuery withAddress(String address) {
        this.address = address;
        return this;
    }

    public String getDbInstanceName() {
        return dbInstanceName;
    }

    public LambdaQuery withDbInstanceName(String dbInstanceName) {
        this.dbInstanceName = dbInstanceName;
        return this;
    }

    public String getDbEngine() {
        return dbEngine;
    }

    public LambdaQuery withDbEngine(String dbEngine) {
        this.dbEngine = dbEngine;
        return this;
    }

    public List<String> getUsers() {
        return users;
    }

    public LambdaQuery withUsers(List<String> users) {
        this.users = users;
        return this;
    }

    public String getUser() {
        return user;
    }

    public LambdaQuery withUser(String user) {
        this.user = user;
        return this;
    }

    public RoleType getRole() {
        return role;
    }

    public LambdaQuery withRole(RoleType role) {
        this.role = role;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public LambdaQuery withPassword(String password) {
        this.password = password;
        return this;
    }

    public Integer getTime() {
        return time;
    }

    public LambdaQuery withTime(Integer time) {
        this.time = time;
        return this;
    }

    public boolean getRdsIAMAuth() {
        return rdsIAMAuth;
    }

    public LambdaQuery withRdsIAMAuth(boolean rdsIAMAuth) {
        this.rdsIAMAuth = rdsIAMAuth;
        return this;
    }
}
