/*
 * Copyright 2018. Gatekeeper Contributors
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

package org.finra.gatekeeper.common.services.account.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

/**
 * Data Transfer Object for Regions (used within Account)
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Region {

    private String name;

    public Region(){}
    public Region(String name){ this.name = name; }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public boolean equals(Object o){
        if(this == o){
            return true;
        }

        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }

        Region that = (Region)o;

        return Objects.equals(this.name, that.getName());
    }

}
