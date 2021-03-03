package org.finra.gatekeeper.services.group.service;

import org.finra.gatekeeper.configuration.GatekeeperRdsAuthProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GatekeeperLdapParseService {

    private GatekeeperRdsAuthProperties gatekeeperRdsAuthProperties;

    @Autowired
    public GatekeeperLdapParseService(GatekeeperRdsAuthProperties gatekeeperRdsAuthProperties){

        this.gatekeeperRdsAuthProperties = gatekeeperRdsAuthProperties;
    }

    /**
     * Takes in a string that represents an AD group and returns an array with the removed values.
     * This parses them into the Application, GK Role, and SDLC and stores them in the array in that order
     * If the String is invalid, it returns an empty String array of length 3
     *
     * @param a String representing an ad group
     * @return A string array of length 3
     * */


    public String[] parseADGroups(String ADgroup){
        if(ADgroup == null){
            return new String[] {"","",""};
        }
        ADgroup = ADgroup.toUpperCase();
        String regex = gatekeeperRdsAuthProperties.getAdGroupsPattern();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ADgroup);

        if(!matcher.find()){
            return new String[] {"","",""};
        }

        return new String[] {matcher.group(1),matcher.group(2),matcher.group(3), matcher.group(0)};
    }
}
