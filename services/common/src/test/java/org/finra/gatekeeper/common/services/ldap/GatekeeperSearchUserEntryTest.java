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

package org.finra.gatekeeper.common.services.ldap;

import org.finra.gatekeeper.common.services.user.model.GatekeeperSearchUserEntry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Objects;

/**
 * Tests for GatekeeperUserEntry
 */
@RunWith(MockitoJUnitRunner.class)
public class GatekeeperSearchUserEntryTest {

    @Test
    public void testConstructorGetter(){
        String id ="user";
        String dn ="someDn";
        String email="user@email.com";
        String name ="User Name";

        GatekeeperSearchUserEntry gatekeeperUserEntry = new GatekeeperSearchUserEntry(id,email,name);

        Assert.assertEquals("Test ID getter", id, gatekeeperUserEntry.getUserId());
        Assert.assertEquals("Test Email getter", email, gatekeeperUserEntry.getEmail());
        Assert.assertEquals("Test Name getter", name, gatekeeperUserEntry.getName());
    }

    @Test
    public void testEquals() throws Exception {
        String id ="user";
        String email="user@email.com";
        String name ="User Name";

        GatekeeperSearchUserEntry gatekeeperUserEntry1 = new GatekeeperSearchUserEntry(id,email,name);
        GatekeeperSearchUserEntry gatekeeperUserEntry2 = new GatekeeperSearchUserEntry(id,email,name);
        GatekeeperSearchUserEntry gatekeeperUserEntry3 = new GatekeeperSearchUserEntry("hello",email,name);
        GatekeeperSearchUserEntry gatekeeperUserEntry5 = new GatekeeperSearchUserEntry(id,"I",name);
        GatekeeperSearchUserEntry gatekeeperUserEntry6 = new GatekeeperSearchUserEntry(id,email,"test");
        GatekeeperSearchUserEntry gatekeeperUserEntry7 = new GatekeeperSearchUserEntry(id,email,"test");

        Assert.assertEquals("Self Check", gatekeeperUserEntry1, gatekeeperUserEntry1);
        Assert.assertEquals("Different but Same Objects", gatekeeperUserEntry1, gatekeeperUserEntry2);
        Assert.assertNotEquals("Different Objects", gatekeeperUserEntry1, "HI");
        Assert.assertNotEquals("Different id", gatekeeperUserEntry1, gatekeeperUserEntry3);
        Assert.assertNotEquals("Different email", gatekeeperUserEntry1, gatekeeperUserEntry5);
        Assert.assertNotEquals("Different name", gatekeeperUserEntry1, gatekeeperUserEntry6);
        Assert.assertNotEquals("Different role", gatekeeperUserEntry1, gatekeeperUserEntry7);
    }

    @Test
    public void testToString(){
        String id ="user";
        String email="user@email.com";
        String name ="User Name";

        GatekeeperSearchUserEntry gatekeeperUserEntry1 = new GatekeeperSearchUserEntry(id,email,name);

        Assert.assertNotNull("ToString returns some kind of string", gatekeeperUserEntry1.toString());
    }

    @Test
    public void testHashCode(){
        String id ="user";
        String email="user@email.com";
        String name ="User Name";

        GatekeeperSearchUserEntry gatekeeperUserEntry1 = new GatekeeperSearchUserEntry(id,email,name);

        Assert.assertEquals("Hashcode", gatekeeperUserEntry1.hashCode(), Objects.hash(id,email,name));

    }
}

