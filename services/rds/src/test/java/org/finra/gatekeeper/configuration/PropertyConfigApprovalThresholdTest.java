package org.finra.gatekeeper.configuration;

import org.finra.gatekeeper.rds.model.RoleType;
import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;
import org.finra.gatekeeper.services.auth.GatekeeperRoleService;
import org.finra.gatekeeper.services.auth.model.AppApprovalThreshold;
import org.finra.gatekeeper.services.auth.model.RoleMembership;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

/**
 * Tests for custom stuff in PropertyConfig
 */

// Exclude the ActivitiSecurityAutoConfiguration class from being initialized because this doesn't work with Spring boot 2
@RunWith(MockitoJUnitRunner.Silent.class)
public class PropertyConfigApprovalThresholdTest {

    @InjectMocks
    private GatekeeperApprovalProperties gatekeeperApprovalProperties;

    @Mock
    private GatekeeperRoleService gatekeeperRoleService;

    private Map<String, RoleMembership> roleMemberships;

    private Set<GatekeeperRdsRole> gatekeeperRdsRoles;

    private Map<String, Map<String, Integer>> devApprovalThreshold;
    private Map<String, Map<String, Integer>> opsApprovalThreshold;
    private Map<String, Map<String, Integer>> dbaApprovalThreshold;
    private Map<String, Map<String, Integer>> secondDevApprovalThreshold;

    private String TEST_AGS = "TESTDEV";
    private String TEST_AGS_2 = "TESTDEV2";

    private String DBA_ROLE = "dba";
    private String DATAFIX_ROLE = "datafix";
    private String READONLY_ROLE = "readonly";
    private String DBA_CONFIDENTIAL_ROLE = "dba_confidential";
    private String READONLY_CONFIDENTIAL_ROLE = "readonly_confidential";

    /**
     * DEV approval thresholds for testing
     */

    private Integer DEV_DBA_DEV_THRESHOLD = 1;
    private Integer DEV_DBA_QA_THRESHOLD = 2;
    private Integer DEV_DBA_PROD_THRESHOLD = 3;

    private Integer DEV_DATAFIX_DEV_THRESHOLD = 4;
    private Integer DEV_DATAFIX_QA_THRESHOLD = 5;
    private Integer DEV_DATAFIX_PROD_THRESHOLD = 6;

    private Integer DEV_READONLY_DEV_THRESHOLD = 7;
    private Integer DEV_READONLY_QA_THRESHOLD = 8;
    private Integer DEV_READONLY_PROD_THRESHOLD = 9;

    /**
     * OPS approval thresholds for testing
     */

    private Integer OPS_DBA_DEV_THRESHOLD = 11;
    private Integer OPS_DBA_QA_THRESHOLD = 12;
    private Integer OPS_DBA_PROD_THRESHOLD = 13;

    private Integer OPS_DATAFIX_DEV_THRESHOLD = 14;
    private Integer OPS_DATAFIX_QA_THRESHOLD = 15;
    private Integer OPS_DATAFIX_PROD_THRESHOLD = 16;

    private Integer OPS_READONLY_DEV_THRESHOLD = 17;
    private Integer OPS_READONLY_QA_THRESHOLD = 18;
    private Integer OPS_READONLY_PROD_THRESHOLD = 19;

    /**
     * DBA approval thresholds for testing
     */

    private Integer DBA_DBA_DEV_THRESHOLD = 21;
    private Integer DBA_DBA_QA_THRESHOLD = 22;
    private Integer DBA_DBA_PROD_THRESHOLD = 23;

    private Integer DBA_DATAFIX_DEV_THRESHOLD = 24;
    private Integer DBA_DATAFIX_QA_THRESHOLD = 25;
    private Integer DBA_DATAFIX_PROD_THRESHOLD = 26;

    private Integer DBA_READONLY_DEV_THRESHOLD = 27;
    private Integer DBA_READONLY_QA_THRESHOLD = 28;
    private Integer DBA_READONLY_PROD_THRESHOLD = 29;

    @Before
    public void init() {
        initRoleMemberships();
        initApprovalThresholds();
    }

    @Test
    public void testGetApprovalPolicyDev() {
        Map<String, AppApprovalThreshold> approvalPolicy = gatekeeperApprovalProperties.getApprovalPolicy(roleMemberships);

        Assert.assertEquals(DEV_DBA_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DBA).get("dev"));
        Assert.assertEquals(DEV_DBA_QA_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DBA).get("qa"));
        Assert.assertEquals(DEV_DBA_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DBA).get("prod"));

        Assert.assertEquals(DEV_DATAFIX_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DATAFIX).get("dev"));
        Assert.assertEquals(DEV_DATAFIX_QA_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DATAFIX).get("qa"));
        Assert.assertEquals(DEV_DATAFIX_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DATAFIX).get("prod"));

        Assert.assertEquals(DEV_READONLY_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.READONLY).get("dev"));
        Assert.assertEquals(DEV_READONLY_QA_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.READONLY).get("qa"));
        Assert.assertEquals(DEV_READONLY_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.READONLY).get("prod"));
    }

    @Test
    public void testGetApprovalPolicyOps() {
        addOpsRoleMemberships();
        initOpsApprovalThresholds();
        
        Map<String, AppApprovalThreshold> approvalPolicy = gatekeeperApprovalProperties.getApprovalPolicy(roleMemberships);

        Assert.assertEquals(OPS_DBA_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DBA).get("dev"));
        Assert.assertEquals(OPS_DBA_QA_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DBA).get("qa"));
        Assert.assertEquals(OPS_DBA_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DBA).get("prod"));

        Assert.assertEquals(OPS_DATAFIX_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DATAFIX).get("dev"));
        Assert.assertEquals(OPS_DATAFIX_QA_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DATAFIX).get("qa"));
        Assert.assertEquals(OPS_DATAFIX_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DATAFIX).get("prod"));

        Assert.assertEquals(OPS_READONLY_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.READONLY).get("dev"));
        Assert.assertEquals(OPS_READONLY_QA_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.READONLY).get("qa"));
        Assert.assertEquals(OPS_READONLY_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.READONLY).get("prod"));
    }

    @Test
    public void testGetApprovalPolicyDba() {
        addOpsRoleMemberships();
        addDbaRoleMemberships();
        initOpsApprovalThresholds();
        initDbaApprovalThresholds();

        Map<String, AppApprovalThreshold> approvalPolicy = gatekeeperApprovalProperties.getApprovalPolicy(roleMemberships);

        Assert.assertEquals(DBA_DBA_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DBA).get("dev"));
        Assert.assertEquals(DBA_DBA_QA_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DBA).get("qa"));
        Assert.assertEquals(DBA_DBA_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DBA).get("prod"));

        Assert.assertEquals(DBA_DATAFIX_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DATAFIX).get("dev"));
        Assert.assertEquals(DBA_DATAFIX_QA_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DATAFIX).get("qa"));
        Assert.assertEquals(DBA_DATAFIX_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DATAFIX).get("prod"));

        Assert.assertEquals(DBA_READONLY_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.READONLY).get("dev"));
        Assert.assertEquals(DBA_READONLY_QA_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.READONLY).get("qa"));
        Assert.assertEquals(DBA_READONLY_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.READONLY).get("prod"));
    }

    @Test
    public void testSecondAgsApprovalThresholds() {
        addOpsRoleMemberships();
        addDbaRoleMemberships();
        initOpsApprovalThresholds();
        initDbaApprovalThresholds();
        initSecondAgs();

        Map<String, AppApprovalThreshold> approvalPolicy = gatekeeperApprovalProperties.getApprovalPolicy(roleMemberships);

        Assert.assertEquals(DBA_DBA_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DBA).get("dev"));
        Assert.assertEquals(DBA_DBA_QA_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DBA).get("qa"));
        Assert.assertEquals(DBA_DBA_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DBA).get("prod"));

        Assert.assertEquals(DBA_DATAFIX_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DATAFIX).get("dev"));
        Assert.assertEquals(DBA_DATAFIX_QA_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DATAFIX).get("qa"));
        Assert.assertEquals(DBA_DATAFIX_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.DATAFIX).get("prod"));

        Assert.assertEquals(DBA_READONLY_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.READONLY).get("dev"));
        Assert.assertEquals(DBA_READONLY_QA_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.READONLY).get("qa"));
        Assert.assertEquals(DBA_READONLY_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS).getAppApprovalThresholds().get(RoleType.READONLY).get("prod"));

        Assert.assertEquals(DEV_DBA_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS_2).getAppApprovalThresholds().get(RoleType.DBA).get("dev"));
        Assert.assertEquals(DEV_DBA_QA_THRESHOLD, approvalPolicy.get(TEST_AGS_2).getAppApprovalThresholds().get(RoleType.DBA).get("qa"));
        Assert.assertEquals(DEV_DBA_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS_2).getAppApprovalThresholds().get(RoleType.DBA).get("prod"));

        Assert.assertEquals(DEV_DATAFIX_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS_2).getAppApprovalThresholds().get(RoleType.DATAFIX).get("dev"));
        Assert.assertEquals(DEV_DATAFIX_QA_THRESHOLD, approvalPolicy.get(TEST_AGS_2).getAppApprovalThresholds().get(RoleType.DATAFIX).get("qa"));
        Assert.assertEquals(DEV_DATAFIX_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS_2).getAppApprovalThresholds().get(RoleType.DATAFIX).get("prod"));

        Assert.assertEquals(DEV_READONLY_DEV_THRESHOLD, approvalPolicy.get(TEST_AGS_2).getAppApprovalThresholds().get(RoleType.READONLY).get("dev"));
        Assert.assertEquals(DEV_READONLY_QA_THRESHOLD, approvalPolicy.get(TEST_AGS_2).getAppApprovalThresholds().get(RoleType.READONLY).get("qa"));
        Assert.assertEquals(DEV_READONLY_PROD_THRESHOLD, approvalPolicy.get(TEST_AGS_2).getAppApprovalThresholds().get(RoleType.READONLY).get("prod"));
    }


    private void initApprovalThresholds() {
        initDevApprovalThresholds();
        initOpsApprovalThresholds();
        initDbaApprovalThresholds();

        ReflectionTestUtils.setField(gatekeeperApprovalProperties,"dev", devApprovalThreshold);
        ReflectionTestUtils.setField(gatekeeperApprovalProperties,"ops", opsApprovalThreshold);
        ReflectionTestUtils.setField(gatekeeperApprovalProperties,"dba", dbaApprovalThreshold);
    }

    private void initDevApprovalThresholds() {
        devApprovalThreshold = new HashMap<>();

        Map<String, Integer> dbaThresholds = new HashMap<>();
        Map<String, Integer> datafixThresholds = new HashMap<>();
        Map<String, Integer> readonlyThresholds = new HashMap<>();
        Map<String, Integer> dbaConfidentialThresholds = new HashMap<>();
        Map<String, Integer> readonlyConfidentialThresholds = new HashMap<>();

        dbaThresholds.put("dev", DEV_DBA_DEV_THRESHOLD);
        dbaThresholds.put("qa", DEV_DBA_QA_THRESHOLD);
        dbaThresholds.put("prod", DEV_DBA_PROD_THRESHOLD);
        datafixThresholds.put("dev", DEV_DATAFIX_DEV_THRESHOLD);
        datafixThresholds.put("qa", DEV_DATAFIX_QA_THRESHOLD);
        datafixThresholds.put("prod", DEV_DATAFIX_PROD_THRESHOLD);
        readonlyThresholds.put("dev", DEV_READONLY_DEV_THRESHOLD);
        readonlyThresholds.put("qa", DEV_READONLY_QA_THRESHOLD);
        readonlyThresholds.put("prod", DEV_READONLY_PROD_THRESHOLD);
        dbaConfidentialThresholds.put("dev", -1);
        dbaConfidentialThresholds.put("qa", -1);
        dbaConfidentialThresholds.put("prod", -1);
        readonlyConfidentialThresholds.put("dev", -1);
        readonlyConfidentialThresholds.put("qa", -1);
        readonlyConfidentialThresholds.put("prod", -1);

        devApprovalThreshold.put(DBA_ROLE, dbaThresholds);
        devApprovalThreshold.put(DATAFIX_ROLE, datafixThresholds);
        devApprovalThreshold.put(READONLY_ROLE, readonlyThresholds);
        devApprovalThreshold.put(READONLY_CONFIDENTIAL_ROLE, readonlyConfidentialThresholds);
        devApprovalThreshold.put(DBA_CONFIDENTIAL_ROLE, dbaConfidentialThresholds);
    }

    private void initOpsApprovalThresholds() {
        opsApprovalThreshold = new HashMap<>();

        Map<String, Integer> dbaThresholds = new HashMap<>();
        Map<String, Integer> datafixThresholds = new HashMap<>();
        Map<String, Integer> readonlyThresholds = new HashMap<>();
        Map<String, Integer> dbaConfidentialThresholds = new HashMap<>();
        Map<String, Integer> readonlyConfidentialThresholds = new HashMap<>();

        dbaThresholds.put("dev", OPS_DBA_DEV_THRESHOLD);
        dbaThresholds.put("qa", OPS_DBA_QA_THRESHOLD);
        dbaThresholds.put("prod", OPS_DBA_PROD_THRESHOLD);
        datafixThresholds.put("dev", OPS_DATAFIX_DEV_THRESHOLD);
        datafixThresholds.put("qa", OPS_DATAFIX_QA_THRESHOLD);
        datafixThresholds.put("prod", OPS_DATAFIX_PROD_THRESHOLD);
        readonlyThresholds.put("dev", OPS_READONLY_DEV_THRESHOLD);
        readonlyThresholds.put("qa", OPS_READONLY_QA_THRESHOLD);
        readonlyThresholds.put("prod", OPS_READONLY_PROD_THRESHOLD);
        dbaConfidentialThresholds.put("dev", -1);
        dbaConfidentialThresholds.put("qa", -1);
        dbaConfidentialThresholds.put("prod", -1);
        readonlyConfidentialThresholds.put("dev", -1);
        readonlyConfidentialThresholds.put("qa", -1);
        readonlyConfidentialThresholds.put("prod", -1);

        opsApprovalThreshold.put(DBA_ROLE, dbaThresholds);
        opsApprovalThreshold.put(DATAFIX_ROLE, datafixThresholds);
        opsApprovalThreshold.put(READONLY_ROLE, readonlyThresholds);
        opsApprovalThreshold.put(READONLY_CONFIDENTIAL_ROLE, readonlyConfidentialThresholds);
        opsApprovalThreshold.put(DBA_CONFIDENTIAL_ROLE, dbaConfidentialThresholds);
    }

    private void initDbaApprovalThresholds() {
        dbaApprovalThreshold = new HashMap<>();

        Map<String, Integer> dbaThresholds = new HashMap<>();
        Map<String, Integer> datafixThresholds = new HashMap<>();
        Map<String, Integer> readonlyThresholds = new HashMap<>();
        Map<String, Integer> dbaConfidentialThresholds = new HashMap<>();
        Map<String, Integer> readonlyConfidentialThresholds = new HashMap<>();

        dbaThresholds.put("dev", DBA_DBA_DEV_THRESHOLD);
        dbaThresholds.put("qa", DBA_DBA_QA_THRESHOLD);
        dbaThresholds.put("prod", DBA_DBA_PROD_THRESHOLD);
        datafixThresholds.put("dev", DBA_DATAFIX_DEV_THRESHOLD);
        datafixThresholds.put("qa", DBA_DATAFIX_QA_THRESHOLD);
        datafixThresholds.put("prod", DBA_DATAFIX_PROD_THRESHOLD);
        readonlyThresholds.put("dev", DBA_READONLY_DEV_THRESHOLD);
        readonlyThresholds.put("qa", DBA_READONLY_QA_THRESHOLD);
        readonlyThresholds.put("prod", DBA_READONLY_PROD_THRESHOLD);
        dbaConfidentialThresholds.put("dev", -1);
        dbaConfidentialThresholds.put("qa", -1);
        dbaConfidentialThresholds.put("prod", -1);
        readonlyConfidentialThresholds.put("dev", -1);
        readonlyConfidentialThresholds.put("qa", -1);
        readonlyConfidentialThresholds.put("prod", -1);

        dbaApprovalThreshold.put(DBA_ROLE, dbaThresholds);
        dbaApprovalThreshold.put(DATAFIX_ROLE, datafixThresholds);
        dbaApprovalThreshold.put(READONLY_ROLE, readonlyThresholds);
        dbaApprovalThreshold.put(READONLY_CONFIDENTIAL_ROLE, readonlyConfidentialThresholds);
        dbaApprovalThreshold.put(DBA_CONFIDENTIAL_ROLE, dbaConfidentialThresholds);
    }

    private void initSecondAgs() {
        Set<String> devSdlcs = new HashSet<>();
        devSdlcs.add("DEV");

        RoleMembership testDev = new RoleMembership();
        testDev.getRoles().put(GatekeeperRdsRole.DEV, devSdlcs);
        roleMemberships.put(TEST_AGS_2, testDev);
    }

    private void initRoleMemberships() {
        roleMemberships = new HashMap<>();
        gatekeeperRdsRoles = new HashSet<>();

        Set<String> devSdlcs = new HashSet<>();
        devSdlcs.add("DEV");

        RoleMembership testDev = new RoleMembership();
        testDev.getRoles().put(GatekeeperRdsRole.DEV, devSdlcs);
        roleMemberships.put(TEST_AGS, testDev);

        gatekeeperRdsRoles.add(GatekeeperRdsRole.DEV);

        Mockito.when(gatekeeperRoleService.getUserRoles(Mockito.any())).thenReturn(gatekeeperRdsRoles);
    }

    private void addOpsRoleMemberships() {

        Set<String> prodSdlcs = new HashSet<>();
        prodSdlcs.add("DEV");
        prodSdlcs.add("QA");
        prodSdlcs.add("PROD");

        roleMemberships.get(TEST_AGS).getRoles().put(GatekeeperRdsRole.OPS, prodSdlcs);

        gatekeeperRdsRoles.add(GatekeeperRdsRole.OPS);

        Mockito.when(gatekeeperRoleService.getUserRoles(Mockito.any())).thenReturn(gatekeeperRdsRoles);
    }

    private void addDbaRoleMemberships() {

        Set<String> prodSdlcs = new HashSet<>();
        prodSdlcs.add("DEV");
        prodSdlcs.add("QA");
        prodSdlcs.add("PROD");

        roleMemberships.get(TEST_AGS).getRoles().put(GatekeeperRdsRole.DBA, prodSdlcs);

        gatekeeperRdsRoles.add(GatekeeperRdsRole.DBA);

        Mockito.when(gatekeeperRoleService.getUserRoles(Mockito.any())).thenReturn(gatekeeperRdsRoles);
    }
}
