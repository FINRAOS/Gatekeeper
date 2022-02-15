package org.finra.gatekeeper.common.services.health;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.finra.gatekeeper.common.properties.GatekeeperHealthProperties;
import org.finra.gatekeeper.common.services.account.AccountInformationService;
import org.finra.gatekeeper.common.services.health.interfaces.DeepHealthCheckItem;
import org.finra.gatekeeper.common.services.health.model.DeepHealthCheckDTO;
import org.finra.gatekeeper.common.services.health.model.DeepHealthCheckTargetDTO;
import org.finra.gatekeeper.common.services.health.model.DeepHealthStatusDTO;
import org.finra.gatekeeper.common.services.health.model.enums.DeepHealthCheckTargetStatus;
import org.finra.gatekeeper.common.services.health.model.enums.DeepHealthStopLight;
import org.finra.gatekeeper.common.services.user.auth.GatekeeperAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This Service is responsible for performing deep health checks as specified for the gatekeeper components
 */

@ConditionalOnBean(GatekeeperHealthProperties.class)
@Service
public class DeepHealthService {

    GatekeeperHealthProperties gatekeeperHealthProperties;
    List<DeepHealthCheckItem> commonHealthChecks;

    @Autowired
    public DeepHealthService(GatekeeperHealthProperties gatekeeperHealthProperties, GatekeeperAuthorizationService gatekeeperAuthorizationService, AccountInformationService accountInfoService){
        this.gatekeeperHealthProperties = gatekeeperHealthProperties;
        this.commonHealthChecks = new ArrayList<>();

        if(gatekeeperHealthProperties.getComponents().contains("ldap")) {
            this.commonHealthChecks.add(gatekeeperAuthorizationService);
        }
        if(gatekeeperHealthProperties.getComponents().contains("accounts")){
            this.commonHealthChecks.add(accountInfoService);
        }
    }

    public Map doDeepHealthCheck(List<DeepHealthCheckItem> checks){
        Map<String, Object> response = new LinkedHashMap<>();
        List<DeepHealthCheckItem> healthChecks = new ArrayList<>(commonHealthChecks);
        healthChecks.addAll(checks);
        ObjectMapper objectMapper = new ObjectMapper();

        List<DeepHealthCheckTargetDTO> results = healthChecks.stream()
                .map(DeepHealthCheckItem::doHealthCheck)
                .collect(Collectors.toList());

        DeepHealthCheckDTO deepHealthCheckDTO = new DeepHealthCheckDTO()
                .setComponent(gatekeeperHealthProperties.getComponentName())
                .setDependencies(results)
                .setRollUpStatus(new DeepHealthStatusDTO().setStatus(
                        results.stream().allMatch(item -> item.getStatus() == DeepHealthCheckTargetStatus.SUCCESS)
                                ? DeepHealthStopLight.GREEN : DeepHealthStopLight.RED)
                );

        response.put(gatekeeperHealthProperties.getTagLabel(), gatekeeperHealthProperties.getTagValue());
        response.putAll(objectMapper.convertValue(deepHealthCheckDTO, Map.class));

        if(!gatekeeperHealthProperties.getTagLabel().equals("application")) {
            ((List<Map<String, Object>>) response.get("dependencies")).forEach(dependency -> {
                dependency.put(gatekeeperHealthProperties.getTagLabel(), dependency.get("application"));
                dependency.remove("application");
            });
        }

        return response;
    }
}
