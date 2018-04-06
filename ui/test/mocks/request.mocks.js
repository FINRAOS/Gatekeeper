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
        {created:'04/15/2016', status:"APPROVED", id:"134", account:"PROD", requestorName:"D K", requestorEmail:"D.K@gk.org", hours:"49",
            users:[
                {"id":8,"name":"S M","email":"S.M@gk.org"},
                {"id":8,"name":"J S","email":"J.S@gk.org"},
                {"id":8,"name":"G C ","email":"G.C@gk.org"},
                {"id":8,"name":"G C","email":"G.C@gk.org"},
                {"id":8,"name":"G C","email":"G.C@gk.org"},
                {"id":8,"name":"A G","email":"A.G@gk.com"}
            ],
            instances:[
                {"id":8,"name":"AWSLXTESTPORTUS","application":"TEST","instanceId":"i-133tm4n5","ip":"192.168.1.2", "status":"Online"},
                {"id":8,"name":"hjdfdffd","application":"TEST","instanceId":"i-435treet","ip":"192.168.1.2"},
                {"id":8,"name":"xcvxvxcvv","application":"TEST","instanceId":"i-545455tt","ip":"192.168.1.2"},
                {"id":8,"name":"vcbccbcbcvbb","application":"TEST","instanceId":"i-4343hhjj","ip":"192.168.1.2"},
                {"id":8,"name":"ghgfghgfhfhh","application":"TEST","instanceId":"i-rrerer33","ip":"192.168.1.2"},
                {"id":8,"name":"nbbdgfdg","application":"TEST","instanceId":"i-rewrwe34","ip":"192.168.1.2"}
            ]},
        {created:'01/15/2016',status:"REJECTED", id:"334", account:"QA", requestorName:"M C", requestorEmail:"M.C@gk.org", hours:"439", users:[{"id":8,"name":"G C","email":"G.C@gk.org"}], instances:[{"id":8,"name":"AWSLXTESTPORTUS","application":"TEST","instanceId":"i-133tm4n5","ip":"192.168.1.2"}]},
        {created:'02/15/2016',status:"APPROVED", id:"434", account:"QA", requestorName:"S M", requestorEmail:"S.M@gk.org", hours:"9001", users:[{"id":8,"name":"G C","email":"G.C@gk.org"}], instances:[{"id":8,"name":"AWSLXTESTPORTUS","application":"TEST","instanceId":"i-133tm4n5","ip":"192.168.1.2"}]},
        {created:'06/15/2016',status:"ERROR", id:"435", account:"PROD", requestorName:"S O", requestorEmail:"SO@gk.org", hours:"1337", users:[{"id":8,"name":"G C","email":"G.C@gk.org"}], instances:[{"id":8,"name":"AWSLXTESTPORTUS","application":"TEST","instanceId":"i-133tm4n5","ip":"192.168.1.2"}]},

    ],

    success : {response: 200}
}