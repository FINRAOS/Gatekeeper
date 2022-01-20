/*
 * Copyright 2022. Gatekeeper Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
