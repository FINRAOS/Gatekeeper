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

import GatekeeperRequestDialogAdminController from './GatekeeperRequestDialogAdminController';
import GatekeeperRequestDialogRequestorController from './GatekeeperRequestDialogRequestorController';

let VM;
let REQUEST = Symbol();

class GatekeeperRequestController {
    constructor(gkRequestService, $rootScope){
        VM = this;
        this[REQUEST] = gkRequestService;
        this.noData = "Currently there are no active requests";

        this.fetched = false;

        this.headerLabel = "Active Requests";
        this.requestTable = {
            selection:'dialog',
            toolbar:{
                header: "Current Active Requests",
                inlineFilter:true
            },
            headers: [
                {dataType:'number', display:'Request ID', value:'id'},
                {dataType:'string', display:'Account', value: 'account'},
                {dataType:'string', display:'Region', value: 'region'},
                {dataType:'string', display:'Requestor Name', value:'requestorName'},
                {dataType:'string', display:'Requestor Email', value:'requestorEmail'},
                {dataType:'number', display:'Hours', value:'hours'},
                {dataType:'number', display:'Users', value:'userCount'},
                {dataType:'number', display:'Instances', value:'instanceCount'}
            ],
            data: [],
            selected: [],
            query:{
                order: 'name',
                limit: 20,
                page: 1
            },
            pagination: {
                pageSelect: true,
                limitOptions: [5, 10, 20, 40]
            },
            responseHandler: function(){

            }
        };
        $rootScope.$on("requestsUpdated", function(){
            VM.getActive();
        });


    }

    getActive(){
        this.requestTable.promise = this[REQUEST].getActive();

        this.requestTable.promise.then((response)=>{
            this.fetched = true;
            this.requestTable.data = response.data;
            this.requestTable.responseHandler(this.requestTable.data);
        }).catch((error)=>{
            this.error = error;
        });
    }
}

export default GatekeeperRequestController;



