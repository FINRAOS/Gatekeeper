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
 */

package org.finra.gatekeeper.services.accessrequest.model;

import com.google.common.base.MoreObjects;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for AccessRequest object
 */
@RunWith(MockitoJUnitRunner.class)
public class UserTest {

    @Test
    public void testConstructor(){
        String userId = "dudes";
        String userName = "The Dude";
        String userEmail = "TheDude@email.com";

        User user = new User(userId, userName, userEmail);

        Assert.assertEquals("Test Id: ", userId, user.getUserId());
        Assert.assertEquals("Test Name:", userName, user.getName());
        Assert.assertEquals("Test Email:", userEmail, user.getEmail());

    }

    @Test
    public void testSetterGetters(){
        Long id = 1L;
        String userId = "dudes";
        String userName = "The Dude";
        String userEmail = "TheDude@email.com";

        User user = new User()
                .setId(id)
                .setUserId(userId)
                .setName(userName)
                .setEmail(userEmail);

        Assert.assertEquals("Test Id: ", id, user.getId());
        Assert.assertEquals("Test User Id: ", userId, user.getUserId());
        Assert.assertEquals("Test User Name:", userName, user.getName());
        Assert.assertEquals("Test User Email:",  userEmail, user.getEmail());

    }

    @Test
    public void testEquals(){
        Long id = 1L;
        String userId = "dudes";
        String userName = "The Dude";
        String userEmail = "TheDude@email.com";

        User user = new User(userId, userName, userEmail);
        user.setId(id);
        User user2 = user;
        User user3 = new User(userId, userName, userEmail);
        user3.setId(id);
        Assert.assertEquals("Same address space", user, user2);
        Assert.assertEquals("Different Objects same values", user, user3);

        /*Negatives*/
        Assert.assertNotEquals("Different Object Types", user, "Hello World");
        Assert.assertNotEquals("Different ids", user.setId(2L), user3);
        user.setId(id);
        Assert.assertNotEquals("Different name", user.setUserId(""), user3);
        user.setUserId(userName);
        Assert.assertNotEquals("Different application", user.setName(""), user3);
        user.setName(userEmail);
        Assert.assertNotEquals("Different instance Id", user.setEmail(""), user3);
        user.setEmail(userEmail);
    }

    @Test
    public void testHashCode(){
        String userId = "dudes";
        String userName = "The Dude";
        String userEmail = "TheDude@email.com";

        User user1 = new User()
                .setId(1L)
                .setUserId(userId)
                .setName(userName)
                .setEmail(userEmail);

        User user2 = new User()
                .setId(2L)
                .setUserId(userId)
                .setName(userName)
                .setEmail(userEmail);

        
        Assert.assertNotEquals(user1.hashCode(), user2.hashCode());

    }

    @Test
    public void testToString(){
        String userId = "dudes";
        String userName = "The Dude";
        String userEmail = "TheDude@email.com";

        User user = new User()
                .setId(1L)
                .setUserId(userId)
                .setName(userName)
                .setEmail(userEmail);


        String exp =  MoreObjects.toStringHelper(User.class)
                .add("ID", 1L)
                .add("User ID", userId)
                .add("Name", userName)
                .add("Email", userEmail)
                .toString();

        Assert.assertEquals("Testing toString()", exp, user.toString());

    }
}
