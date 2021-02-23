package org.finra.gatekeeper.services.group.interfaces;

import org.finra.gatekeeper.common.services.user.model.GatekeeperUserEntry;
import org.finra.gatekeeper.controllers.wrappers.AccessRequestWrapper;

public interface IGatekeeperGroupAuthService {
    public boolean hasGroupAuth(AccessRequestWrapper request, GatekeeperUserEntry requestor);
}
