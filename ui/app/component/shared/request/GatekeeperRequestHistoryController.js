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

import GatekeeperRequestDialogController from './GatekeeperRequestDialogController';

let REQUEST = Symbol();
let vm;

class GatekeeperRequestHistoryController {
    constructor(gkRequestService, $rootScope){
        vm = this;
        this[REQUEST] = gkRequestService;
        this.noData = "Currently there are no request history items";
        this.fetched = false;
        this.headerLabel = "Request History";
        this.requestTable = {
            selection:'dialog',
            templateController: GatekeeperRequestDialogController,
            templateControllerAs: 'dialogCtrl',
            toolbar:{
                header: "Recently Handled Requests",
                inlineFilter:true,
                selectFilters: [
                    {
                        label: 'Environment',
                        filterFn: vm.filterEnvironment,
                        options: [],
                        width: '118px'
                    }]
            },
            headers:[
                {dataType:'date',  config:{dateFormat:'short'}, display:'Created', value:'created'},
                {dataType:'date',  config:{dateFormat:'short'}, display:'Updated', value:'updated'},
                {dataType:'string',display:'Request ID', value:'id'},
                {dataType:'string',display:'Environment/Account', value: 'account'},
                {dataType:'string',display:'Requestor Name', value:'requestorName'},
                {dataType:'string',display:'Requestor Email', value:'requestorEmail'},
                {dataType:'number',display:'Hours Requested', value:'hours'},
                {dataType:'number',display:'Users', value:'userCount'},
                {dataType:'number',display:'Instances', value:'instanceCount'},
                {dataType:'string',display:'Outcome', value:'status'}
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
    }

    filterEnvironment(input, row){
        return row.account === input;
    }

    setAccountOptions(data){
        let options = new Set();
        for (let i of data){
            options.add(i.account);
        }
        let array = Array.from(options).sort();
        array.unshift('ALL');
        this.requestTable.toolbar.selectFilters[0].options = array;
    }

    getCompleted(){
        this.requestTable.promise = this[REQUEST].getCompleted();
        this.requestTable.promise.then((response)=>{
            this.fetched = true;
            this.requestTable.data = response.data;
            this.requestTable.responseHandler(this.requestTable.data);
            this.setAccountOptions(this.requestTable.data);
        }).catch((error)=>{
            this.error = error;
        });
    }
}

export default GatekeeperRequestHistoryController;



