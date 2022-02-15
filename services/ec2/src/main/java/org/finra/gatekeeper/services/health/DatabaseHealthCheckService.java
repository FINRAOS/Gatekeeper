package org.finra.gatekeeper.services.health;

import org.finra.gatekeeper.common.properties.GatekeeperHealthProperties;
import org.finra.gatekeeper.common.services.health.interfaces.DeepHealthCheckItem;
import org.finra.gatekeeper.common.services.health.model.DeepHealthCheckTargetDTO;
import org.finra.gatekeeper.common.services.health.model.enums.DeepHealthCheckTargetStatus;
import org.finra.gatekeeper.common.services.health.model.enums.DependencyCriticality;
import org.finra.gatekeeper.services.accessrequest.model.AccessRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * This service just performs a query against the database to ensure connection to the data source
 */
@Service
public class DatabaseHealthCheckService implements DeepHealthCheckItem {

    @Value("${gatekeeper.db.url}")
    private String endpoint;
    private GatekeeperHealthProperties healthProperties;
    private AccessRequestRepository repository;

    @Autowired
    public DatabaseHealthCheckService(GatekeeperHealthProperties healthProperties, AccessRequestRepository securityPolicyRepository){
        this.healthProperties = healthProperties;
        this.repository = securityPolicyRepository;
    }

    @Override
    public DeepHealthCheckTargetDTO doHealthCheck() {
        DeepHealthCheckTargetDTO deepHealthCheckTargetDTO = new DeepHealthCheckTargetDTO()
                .setApplication(healthProperties.getDatabaseTag())
                .setUri(endpoint)
                .setCategory("database")
                .setDependencyType(DependencyCriticality.REQUIRED)
                .setDescription("This checks whether Gatekeeper can connect successfully with its Database")
                .setComponent(healthProperties.getDatabaseComponent())
                .setStartTimestamp(LocalDateTime.now().toString());
        try{
            repository.healthCheck();
            deepHealthCheckTargetDTO.setStatus(DeepHealthCheckTargetStatus.SUCCESS);
        } catch (Exception e) {
            deepHealthCheckTargetDTO.setExceptionMessage(e.getMessage())
                    .setStatus(DeepHealthCheckTargetStatus.FAILURE);
        }
        return deepHealthCheckTargetDTO
                .setEndTimestamp(LocalDateTime.now().toString());
    }
}
