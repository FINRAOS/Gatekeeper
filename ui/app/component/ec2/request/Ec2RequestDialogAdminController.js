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

import GatekeeperRequestDialogAdminController from '../../shared/request/GatekeeperRequestDialogAdminController';


class Ec2RequestDialogAdminController extends GatekeeperRequestDialogAdminController {
    constructor($rootScope, $mdDialog, $mdToast, gkRequestService, row){
        super($rootScope, $mdDialog, $mdToast, gkRequestService, row);

        let approve = {
            label:'Approve',
            action:this.approveRequest,
            style: 'md-raised md-accent'
        };

        let reject = {
            label:'Reject',
            action:this.rejectRequest,
            style: 'md-raised md-primary'
        };

        this.isBadRequest = row.instances.every((item) => {
            return item.status !== "Online";
        });

        if(this.isBadRequest){
            this.actions.unshift(this.cancel);
        }else {
            this.actions.unshift(approve, reject)
        }
    }


}

export default Ec2RequestDialogAdminController