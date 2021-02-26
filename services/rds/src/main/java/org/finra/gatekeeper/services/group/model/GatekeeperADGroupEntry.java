package org.finra.gatekeeper.services.group.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GatekeeperADGroupEntry {
    private String AGS;
    private String GK_ROLE;
    private String SDLC;
    private String NAME;

    public GatekeeperADGroupEntry(String AGS, String GK_ROLE, String SDLC, String NAME){
        this.AGS = AGS;
        this.GK_ROLE = GK_ROLE;
        this.SDLC = SDLC;
        this.NAME = NAME;
    }

    public String getAGS() {
        return AGS;
    }

    public String getGK_ROLE() {
        return GK_ROLE;
    }

    public String getSDLC() {
        return SDLC;
    }

    public String getNAME() {return NAME;}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GatekeeperADGroupEntry that = (GatekeeperADGroupEntry) obj;
        return Objects.equal(AGS, that.AGS) &&
               Objects.equal(GK_ROLE, that.GK_ROLE) &&
               Objects.equal(SDLC, that.SDLC) &&
                Objects.equal(NAME, that.NAME);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(AGS, GK_ROLE, SDLC, NAME);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("AGS", AGS)
                .add("GK ROLE", GK_ROLE)
                .add("SDLC", SDLC)
                .add("NAME", NAME)
                .toString();
    }


}
