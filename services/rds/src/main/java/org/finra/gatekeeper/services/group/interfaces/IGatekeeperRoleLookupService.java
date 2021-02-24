package org.finra.gatekeeper.services.group.interfaces;

import org.finra.gatekeeper.services.accessrequest.model.User;
import org.finra.gatekeeper.services.group.model.GatekeeperADGroupEntry;

import java.util.Map;
import java.util.Set;

/**
 * Interface that is expected to handle user lookup to be used with gatekeeper
 */
public interface IGatekeeperRoleLookupService {
    public Map<String, Set<GatekeeperADGroupEntry>> loadRoles(String userId);
}
