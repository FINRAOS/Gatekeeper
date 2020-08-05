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

import GatekeeperRequestHistoryController from '../../shared/request/GatekeeperRequestHistoryController';


class Ec2RequestHistoryController extends GatekeeperRequestHistoryController{
    constructor(gkRequestService){
        super(gkRequestService)

        this.requestTable.template = require('./template/request.tpl.html');

        // this.requestTable.responseHandler = function(data){
        //     data.forEach(function(row){
        //         row.instances.forEach(function(instance){
        //             instance.icon = instance.status === 'Online' ?  'hardware:computer' :'notification:sync_problem';
        //         });
        //     });
        // };

        this.getCompleted();
    }
}

export default Ec2RequestHistoryController;



