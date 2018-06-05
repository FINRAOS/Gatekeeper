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

const TOAST = Symbol();
const AWS = Symbol();
const GRANT = Symbol();

//need this to deal with callbacks
let vm;

import GatekeeperSubmissionDialogController from '../../shared/selfservice/GatekeeperSubmissionDialogController';
import GatekeeperSelfServiceController from '../../shared/selfservice/GatekeeperSelfServiceController';

class Ec2SelfServiceController extends GatekeeperSelfServiceController {
    constructor($mdDialog, $mdToast, gkADService, gkAWSService, gkGrantService, gkAccountService,$scope,$state,$rootScope){
        super($mdDialog, $mdToast, gkADService,$scope,$state,$rootScope);
        vm = this;
        vm.global = $rootScope;

        this[AWS] = gkAWSService;
        this[GRANT] = gkGrantService;
        this[TOAST] = $mdToast;

        this.awsInstanceFilter = {
            onlineOnly:false
        };

        this.platforms = ['Linux', 'Windows'];
        this.awsAccounts = [];

        this.fetching.aws = false;


        //aws stuff
        this.awsSearchableTags = [
            'Instance ID', 'Name', 'IP', 'Application'
        ];

        gkAccountService.fetch().then((response) =>{
            this.awsAccounts = response.data;
        }).catch(()=>{
            throw new Error('Error fetching accounts');
        });

        this.awsTable = {
            selection: 'multiple',
            selectionId: 'instanceId',
            toolbar: {
                header: "Search Result",
                inlineFilter: true,
                checkboxFilters: [
                    {
                        label: "SSM Online Only",
                        filterFn: this.filterOnline
                    },
                    {
                        label: "Show Offline Only",
                        filterFn: this.filterOffline
                    }
                ]
            },
            onSelect: this.checkIfApprovalNeeded,
            onDeselect: this.checkIfApprovalNeeded,
            headers: [
                {dataType: 'string', display: 'Instance ID', value: 'instanceId'},
                {dataType: 'string', display: 'Instance Name', value: 'name'},
                {dataType: 'string', display: 'Application', value: 'application'},
                {dataType: 'string', display: 'Platform', value: 'platform'},
                {dataType: 'string', display: 'Instance IP', value: 'ip'},
                {dataType: 'string', display: 'SSM Status', value: 'ssmStatus'}
            ],
            data: [],
            selected: [],
            query: {
                order: 'name',
                limit: 5,
                page: 1
            },
            pagination: {
                pageSelect: true,
                limitOptions: [5, 10]
            },
            disableRow: this.disableRow,
            disableBackgroundColor: 'rgba(0,0,0,0.12)'
        };

    }

    /**
     * Called from the UI to clear out the AWS Instances if a change in region or account is made.
     */
    clearInstances(){
        let vm = this;
        let title = 'Change Account/Region/Platform';
        let message = 'Changing Account, Region, or Platform will clear out your currently selected instances. Are you sure?';

        //only need to do anything if the user has anything selected
        if(this.awsTable.selected.length > 0) {
            vm.dialogOpened = true;
            vm.spawnConfirmDialog(title, message)
                .then(() => {
                    vm.awsTable.selected = [];
                    vm.awsTable.data = [];
                    if(vm.awsInstanceForm.selectedAccount !== vm.lastSelectedAccount){
                        delete vm.awsInstanceForm.selectedRegion;
                    }
                    vm.awsInstanceForm.$setUntouched();
                    vm.awsInstanceForm.$setPristine();
                    vm.checkIfApprovalNeeded();
                }).catch(() => {
                    //Do nothing, essentially
                    vm.awsInstanceForm.selectedAccount = vm.lastSelectedAccount;
                    vm.awsInstanceForm.selectedRegion = vm.lastSelectedRegion;
                    vm.awsInstanceForm.selectedPlatform = vm.lastSelectedPlatform;
                }).finally(() => {
                    vm.dialogOpened = false;
                })
        }else{
            vm.lastSelectedAccount = vm.awsInstanceForm.selectedAccount;
            vm.lastSelectedRegion = vm.awsInstanceForm.selectedRegion;
            vm.lastSelectedPlatform = vm.awsInstanceForm.selectedPlatform;
            vm.awsTable.data = [];
            vm.checkIfApprovalNeeded();
        }

    }

    disableRow(row){
        return row.ssmStatus !== 'Online';
    }


    filterOnline(row){
        return row.ssmStatus === 'Online';
    }

    filterOffline(row){
        return !vm.filterOnline(row);
    }

    searchAWSInstances(){
        if(this.awsInstanceForm.$valid) {
            delete this.error.aws;
            this.fetching.aws = true;
            this.awsTable.data = [];
            this.awsTable.promise = this[AWS].search(
                {
                    account: this.awsInstanceForm.selectedAccount.alias.toLowerCase(),
                    region: this.awsInstanceForm.selectedRegion.name,
                    searchTag:this.awsInstanceForm.searchTag,
                    searchStr:this.awsInstanceForm.searchText,
                    platform:this.awsInstanceForm.selectedPlatform
                });

            this.awsTable.promise.then((response) => {
                this.awsTable.data = response.data;
            }).catch((error) =>{
                this.error.aws = error;
            }).finally(() =>{
                this.fetching.aws = false;
            });
        }
    }

    checkIfApprovalNeeded(){
        vm.approvalRequired = false;
        if(vm.global.userInfo.role !== 'APPROVER') {
            //first check hours
            if (vm.awsInstanceForm.selectedAccount) {
                vm.approvalRequired = vm.grantForm.grantValue > vm.global.userInfo.approvalThreshold[vm.awsInstanceForm.selectedAccount.sdlc.toLowerCase()]
            }
            //if the hours didn't cross the threshold then check application on selected instances
            if (!vm.approvalRequired && (vm.global.userInfo.role === 'DEV' || vm.global.userInfo.role === 'OPS')) {
                vm.awsTable.selected.forEach((item) => {
                    if (!vm.approvalRequired) {
                        vm.approvalRequired = vm.global.userInfo.memberships.indexOf(item.application) === -1;
                    }
                })
            }
        }
    }

    grantAccess(){
        let vm = this;
        if(this.grantForm.$valid) {
            delete vm.error.request;
            let title = 'Confirm Access Request';
            let message = 'This will request access for ' + vm.grantForm.grantValue + ' hour(s) for the selected users and instances. ';
            if(vm.approvalRequired){
                message += 'This request will require approval.'
            }

            let config = {title:title, message:message, requiresExplanation:vm.approvalRequired};
            vm.spawnTemplatedDialog(config)
                .then((explanation) => {
                    this.fetching.grant = true;
                    vm[GRANT].post(vm.grantForm.grantValue, vm.usersTable.selected, vm.awsInstanceForm.selectedAccount.alias.toLowerCase(), vm.awsInstanceForm.selectedRegion.name, vm.awsTable.selected, explanation, vm.awsInstanceForm.selectedPlatform)
                        .then((response) => {
                            this.fetching.grant = false;
                            let msg = 'Access was requested for ' + vm.grantForm.grantValue + ' hours. If your request required approval,'
                                + ' access will not be granted until your request is reviewed and actioned by an approver. Once granted, users will' +
                                ' be sent an email with further instructions';
                            vm[TOAST].show(
                                vm[TOAST].simple()
                                    .textContent(msg)
                                    .position('bottom right')
                                    .hideDelay(10000)
                            );
                            //deselect all users and instances
                            vm.awsTable.selected = [];
                            vm.usersTable.selected = [];
                            vm.selfService = false;
                            delete vm.grantForm.grantValue;

                            //needed since forms are still dirty
                            vm.confirm=false;
                        }).catch((error) => {
                        this.fetching.grant = false;
                        let msg = 'There was an error while trying to request access. Please make sure the requested instances are in a running state and are able to access AWS APIs.';
                        vm.error.request = error;
                        vm[TOAST].show(
                            vm[TOAST].simple()
                                .textContent(msg)
                                .position('bottom right')
                                .hideDelay(10000)
                        );
                    });
                });
        }
    }
}

export default Ec2SelfServiceController;


