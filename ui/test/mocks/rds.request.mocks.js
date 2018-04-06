/*
 *
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

export default {
    requestResp :  [
        {
            "id":51,
            "hours":80,
            "requestorId":"js",
            "requestorName":"J S",
            "requestorEmail":"J.S@gk.org",
            "account":"DEV",
            "region":"us-east-1",
            "requestReason":null,
            "approverComments":null,
            "users":[
                {
                    "id":53,
                    "name":"J S",
                    "email":"J.S@gk.org",
                    "userId":"gk-js"
                }
            ],
            "awsRdsDatabases":[
                {
                    "id":52,
                    "name":"rds-instance-1",
                    "application":"TEST",
                    "instanceId":"test-instanceID",
                    "engine":"testEngine",
                    "url":"dbUrl-rds",
                    "arn":"some:kinda:ARN",
                    "status":"Available"
                }
            ],
            "userCount":1,
            "instanceCount":1,
            "taskId":"17611",
            "created":1465915084686
        }
    ],

    success : {response: 200}
}