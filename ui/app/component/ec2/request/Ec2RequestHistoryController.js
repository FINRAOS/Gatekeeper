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
import Ec2RequestHistoryAdminController from './Ec2RequestHistoryAdminController';

class Ec2RequestHistoryController extends GatekeeperRequestHistoryController{
    constructor(gkRequestService, $rootScope){
        super(gkRequestService, $rootScope);
        let isApprover = $rootScope.userInfo.isApprover;
        if(isApprover){
            this.requestTable.templateController = Ec2RequestHistoryAdminController;
        }

        this.requestTable.template = require('./template/request.tpl.html');

        this.getCompleted();
    }
}

export default Ec2RequestHistoryController;



