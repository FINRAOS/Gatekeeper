package org.finra.gatekeeper.controllers;


import org.finra.gatekeeper.common.properties.GatekeeperHealthProperties;
import org.finra.gatekeeper.common.services.health.DeepHealthService;
import org.finra.gatekeeper.common.services.health.interfaces.DeepHealthCheckItem;
import org.finra.gatekeeper.services.health.DatabaseHealthCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ConditionalOnBean(GatekeeperHealthProperties.class)
@RestController
@RequestMapping("/status")
public class DeepHealthController {

    private DeepHealthService deepHealthService;
    private List<DeepHealthCheckItem> deepHealthChecks;

    @Autowired
    private DeepHealthController(DeepHealthService deepHealthService, DatabaseHealthCheckService databaseHealthCheckService){
        this.deepHealthService = deepHealthService;
        this.deepHealthChecks = Arrays.asList(databaseHealthCheckService);
    }

    @ResponseBody
    @GetMapping(value="/depcheck")
    public Map deepHealthCheck(){
        return deepHealthService.doDeepHealthCheck(deepHealthChecks);
    }
    
}
