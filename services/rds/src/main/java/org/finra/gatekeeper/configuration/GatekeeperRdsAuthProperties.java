package org.finra.gatekeeper.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="gatekeeper.auth")
public class GatekeeperRdsAuthProperties {

    /**
     * A regular expression to capture the groups the dba is under.
     *
     * You need to provide an area to capture (example: GROUP_([a-zA-Z]+)_DBADMIN)
     */
    private String dbaGroupsPattern;

    /**
     * A regular expression to capture the groups the ops team member is under.
     *
     * You need to provide an area to capture (example: GROUP_([a-zA-Z]+)_OPERATIONS)
     */

    private String opsGroupsPattern;

    /**
     * A regular expression to capture the groups the dev team member is under.
     *
     * You need to provide an area to capture (example: GROUP_([a-zA-Z]+)_DEVELOPER)
     */
    private String devGroupsPattern;


    /**
     * A regular expression to capture the gatekeeper groups from ldap
     *
     * You need to proide an area to capture (example: "APP_GK_([A-Z]{2,8})_(RO|DF|DBA|ROC|DBAC)_(Q|D|P)")
     *
     * */

    private String adGroupsPattern;
    /**
     * What SDLC's should have the restrictive filter disabled
     *
     *  Should Enter D, Q, or P delimited by a comma  (example: D, Q, P)
     */
    private String unrestrictedSDLC;

    public String getDbaGroupsPattern() {
        return dbaGroupsPattern;
    }

    public GatekeeperRdsAuthProperties setDbaGroupsPattern(String dbaGroupsPattern) {
        this.dbaGroupsPattern = dbaGroupsPattern;
        return this;
    }

    public String getOpsGroupsPattern() {
        return opsGroupsPattern;
    }

    public GatekeeperRdsAuthProperties setOpsGroupsPattern(String opsGroupsPattern) {
        this.opsGroupsPattern = opsGroupsPattern;
        return this;
    }

    public String getDevGroupsPattern() {
        return devGroupsPattern;
    }

    public GatekeeperRdsAuthProperties setDevGroupsPattern(String devGroupsPattern) {
        this.devGroupsPattern = devGroupsPattern;
        return this;
    }
    public String getAdGroupsPattern() {
        return adGroupsPattern;
    }

    public void setAdGroupsPattern(String adGroupsPattern) {
        this.adGroupsPattern = adGroupsPattern;
    }

    public char[] getUnrestrictedSDLC() {
        if(unrestrictedSDLC == null){
            char[] empty = new char[1];
            empty[0] = ' ';
            return empty;
        }
        String trimmed = unrestrictedSDLC.toUpperCase().replaceAll("[^DQP] ", "");
        if(trimmed.length() > 3){
            trimmed.substring(0, 2);
        }
        char[] sdlcs = trimmed.toCharArray();
        return sdlcs;
    }

    public void setUnrestrictedSDLC(String unrestrictedSDLC) {
        this.unrestrictedSDLC = unrestrictedSDLC;
    }
}
