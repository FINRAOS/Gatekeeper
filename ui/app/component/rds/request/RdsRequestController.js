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

import GatekeeperRequestController from '../../shared/request/GatekeeperRequestController';
import RdsRequestDialogAdminController from './RdsRequestDialogAdminController';
import GatekeeperRequestDialogRequestorController from '../../shared/request/GatekeeperRequestDialogRequestorController';


let VM;
let ROLES = {
    approver: 'APPROVER',
    developer: 'DEV',
    operations: 'OPS',
    support:'SUPPORT',
    unauthorized: 'UNAUTHORIZED'
};


class RdsRequestController extends GatekeeperRequestController{
    constructor(gkRequestService, $rootScope){
        super(gkRequestService, $rootScope);
        VM = this;
        let isApprover = $rootScope.userInfo.isApprover;

        this.requestTable.template = require('./template/request.tpl.html');
        this.requestTable.templateController = isApprover ? RdsRequestDialogAdminController : GatekeeperRequestDialogRequestorController;
        this.requestTable.templateControllerAs=  'dialogCtrl';

        this.requestTable.headers = [
            {dataType:'number', display:'Request ID', value:'id'},
            {dataType:'string', display:'Account', value: 'account'},
            {dataType:'string', display:'Region', value: 'region'},
            {dataType:'string', display:'Requestor Name', value:'requestorName'},
            {dataType:'string', display:'Requestor Email', value:'requestorEmail'},
            {dataType:'number', display:'Days', value:'days'},
            {dataType:'number', display:'Users', value:'userCount'},
            {dataType:'number', display:'Instances', value:'instanceCount'}
        ];

        this.requestTable.responseHandler = function(data){
            data.forEach(function(row){
                row.instances.forEach(function(instance){
                    instance.icon = instance.status === 'Available' ?  'device:storage' :'notification:sync_problem';
                });
            });
        };

        this.getActive();

        $rootScope.$on("requestsUpdated", function(){
            VM.getActive();
        })


    }

}

export default RdsRequestController;



