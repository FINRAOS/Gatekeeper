package org.finra.gatekeeper.services.auth.model;

import org.finra.gatekeeper.services.auth.GatekeeperRdsRole;

import java.util.*;

public class RoleMembership {
    private Map<GatekeeperRdsRole, Set<String>> roles;

    public RoleMembership() {
        roles = new HashMap<>();
    }

    public RoleMembership(Map<GatekeeperRdsRole, Set<String>> roles) {
        this.roles = roles;
    }

    public Map<GatekeeperRdsRole, Set<String>> getRoles() {
        return roles;
    }

    public void setRoles(Map<GatekeeperRdsRole, Set<String>> roles) {
        this.roles = roles;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleMembership that = (RoleMembership) o;
        return Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roles);
    }

    @Override
    public String toString() {
        return "RoleMembership{" +
                "roles=" + roles +
                '}';
    }
}
