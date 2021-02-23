package org.finra.gatekeeper.services.group.interfaces;

import org.finra.gatekeeper.services.group.model.GatekeeperADGroupEntry;

import java.util.Map;
import java.util.Set;

/**
 * Interface that is expected to handle user lookup to be used with gatekeeper
 */
public interface IGatekeeperGroupLookupService {
    public Map<String, Set<GatekeeperADGroupEntry>> loadGroups();
}
