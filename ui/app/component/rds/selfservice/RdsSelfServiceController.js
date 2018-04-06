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

const AD = Symbol();
const DIALOG = Symbol();
const TOAST = Symbol();
const RDS = Symbol();
const GRANT = Symbol();
let STATE = Symbol();

//need this to deal with callbacks
let vm;

import GatekeeperSelfServiceController from '../../shared/selfservice/GatekeeperSelfServiceController';

class RdsSelfServiceController extends GatekeeperSelfServiceController {
    constructor($mdDialog, $mdToast, gkADService, gkRDSService, gkRdsGrantService, gkAccountService,$scope,$state,$rootScope){
        super($mdDialog, $mdToast, gkADService,$scope,$state,$rootScope);

        vm = this;
        this[RDS] = gkRDSService;
        this[GRANT] = gkRdsGrantService;
        this[TOAST] = $mdToast;
        this[DIALOG] = $mdDialog;

        gkAccountService.fetch().then((response) =>{
            this.awsAccounts = response.data;
        }).catch(()=>{
            throw new Error('Error fetching accounts');
        });

        this.rdsInstanceFilter = {
            onlineOnly:false
        };

        this.awsAccounts = [];

        this.fetching.rds = false;

        this.awsTable = {
            selection: 'multiple',
            selectionId: 'instanceId',
            toolbar: {
                header: "Available Databases",
                inlineFilter: true,
                checkboxFilters: [
                    {
                        label: "Available Only",
                        filterFn: this.filterOnline
                    },
                    {
                        label: "Unavailable Only",
                        filterFn: this.filterOffline
                    }
                ]
            },
            onSelect: this.checkIfApprovalNeeded,
            onDeselect: this.checkIfApprovalNeeded,
            headers: [
                {dataType: 'string', display: 'Instance Name', value: 'name'},
                {dataType: 'string', display: 'Database Name', value: 'dbName'},
                {dataType: 'string', display: 'Engine',        value: 'engine'},
                {dataType: 'string', display: 'Instance ID',   value: 'instanceId'},
                {dataType: 'string', display: 'Status',        value: 'status'}
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
        return row.status !== 'available';
    }

    filterOnline(row){
        return row.status === 'available';
    }

    filterOffline(row){
        return !vm.filterOnline(row);
    }

    searchRDSInstances(){
        if(this.awsInstanceForm.$valid) {
            delete this.error.aws;
            this.fetching.rds = true;
            this.awsTable.data = [];
            this.awsTable.promise = this[RDS].search(
                {
                    account: this.awsInstanceForm.selectedAccount.alias.toLowerCase(),
                    region: this.awsInstanceForm.selectedRegion.name,
                    searchText:this.awsInstanceForm.searchText,
                });

            this.awsTable.promise.then((response) => {
                this.awsTable.data = response.data;
            }).catch((error) =>{
                this.error.aws = error;
            }).finally(() =>{
                this.fetching.rds = false;
            });
        }
    }

    isFormValid(){
        let valueChecked = false;

        if(vm.grantForm && vm.grantForm.selectedRoles) {
            angular.forEach(vm.grantForm.selectedRoles, (v, k) => {
                if (v) {
                    valueChecked = true;
                }
            });
        }

        return vm.usersTable.selected.length === 0 || vm.awsTable.selected.length === 0 || !valueChecked
    }

    checkIfApprovalNeeded(){
        return vm.grantForm.grantValue > vm.getApprovalBounds() || vm.getApprovalBounds() === -1;
    }

    //Get the lowest threshold value across all selected roles for provided sdlc
    getApprovalBounds(){

        //approvers don't need approval
        if(vm.global.userInfo.role.toLowerCase() == 'approver'){
            return 180;
        }

        let approvalRequired = true;
        //first lets make sure they are members of what they are requesting for
        vm.awsTable.selected.forEach((item) => {
            let resource = vm.global.userInfo.memberships[item.application];
            approvalRequired = !resource || resource.indexOf(vm.awsInstanceForm.selectedAccount.sdlc.toUpperCase()) === -1;
        });

        //if the user isn't part of what he is requesting for warn for approval.
        if(approvalRequired){
            return -1;
        }

        let values = [];

        angular.forEach(vm.grantForm.selectedRoles, (v, k) => {
            if(v){
                values.push(vm.global.userInfo.approvalThreshold[k][vm.awsInstanceForm.selectedAccount.sdlc.toLowerCase()]);
            }
        });

        return  Math.min.apply(Math, values)

    }

    showSchemas(chip){
        let config = {
            controller: 'gkRdsSchemaDialogController',
            controllerAs: 'schemaCtrl',
            clickOutsideToClose: true,
            title: chip.name + " (" + chip.engine + ") ",
            template: require("./template/schemas.tpl.html"),
            parent: angular.element(document.body),
            locals: {
                account: vm.awsInstanceForm.selectedAccount,
                region: vm.awsInstanceForm.selectedRegion,
                database: chip
            }
        };
        vm[DIALOG].show(config);
    }

    getMaximumDays(){

        let max = 180;
        if(vm.awsInstanceForm.selectedAccount) {
            if (vm.awsInstanceForm.selectedAccount.sdlc === 'prod' && vm.getSelectedRoles().indexOf('dba') !== -1) {
                max = 7;
            }

            if (vm.awsInstanceForm.selectedAccount.sdlc === 'prod' && vm.getSelectedRoles().indexOf('datafix') !== -1) {
                max = 1;
            }
        }
        return max;
    }

    getSelectedRoles() {
        let selectedRoles = [];
        angular.forEach(vm.grantForm.selectedRoles, (value, key) => {
            if(value){
                selectedRoles.push(key);
            }
        });

        return selectedRoles;
    }

    grantAccess(){
        let vm = this;
        if(this.grantForm.$valid) {
            delete vm.error.request;
            let title = 'Confirm Access Request';
            let message = 'This will request access for ' + vm.grantForm.grantValue + ' day(s) for the selected users and instances. ';
            let approvalRequired = vm.checkIfApprovalNeeded();
            if(approvalRequired){
                message += 'This request will require approval.'
            }

            let config = {title:title, message:message, requiresExplanation: approvalRequired};
            vm.spawnTemplatedDialog(config)
                .then((explanation) => {
                    this.fetching.grant = true;
                    let roles = [];
                    vm[GRANT].post(vm.getSelectedRoles(), vm.grantForm.grantValue, vm.usersTable.selected, vm.awsInstanceForm.selectedAccount.alias.toLowerCase(), vm.awsInstanceForm.selectedAccount.sdlc.toLowerCase(), vm.awsInstanceForm.selectedRegion.name, vm.awsTable.selected, explanation, vm.awsInstanceForm.selectedPlatform)
                        .then((response) => {
                            this.fetching.grant = false;
                            var msg;
                            if(response.data.outcome === 'CREATED') {
                                msg = 'Access was requested for ' + vm.grantForm.grantValue + ' days. If your request required approval,'
                                    + ' access will not be granted until your request is reviewed and actioned by an approver. Once granted, users will' +
                                    ' be sent an email with further instructions';
                            }else{
                                msg = 'Access was NOT requested due to old/expired users having temporary tables on the databases that were selected, please work with the Ops team to get these users cleaned up.';
                            }
                            vm[TOAST].show(
                                vm[TOAST].simple()
                                    .textContent(msg)
                                    .position('bottom right')
                                    .hideDelay(10000)
                            );
                            if(response.data.outcome === 'CREATED') {
                                //deselect all users and instances
                                vm.awsTable.selected = [];
                                vm.usersTable.selected = [];
                                vm.selfService = false;
                                delete vm.grantForm.grantValue;

                                //needed since forms are still dirty
                                vm.confirm = false;
                            }else{
                                vm.error.databases = response.data.response;
                            }
                        }).catch((error) => {
                            this.fetching.grant = false;
                            let msg = 'There was an error while trying to request access. Please make sure the requested instances are in a running state and are able to access AWS APIs.';
                            vm.error.request = error;
                            vm[TOAST].show(
                                vm[TOAST].simple()
                                    .textContent(msg)
                                    .position('bottom right')
                                    .hideDelay(10000));
                    })
            });
        }
    }
}

export default RdsSelfServiceController;