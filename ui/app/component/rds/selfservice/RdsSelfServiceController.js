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

const DIALOG = Symbol();
const TOAST = Symbol();
const GRANT = Symbol();

//need this to deal with callbacks
let vm;

import GatekeeperSelfServiceController from '../../shared/selfservice/GatekeeperSelfServiceController';

class RdsSelfServiceController extends GatekeeperSelfServiceController {
    constructor($mdDialog, $mdToast, gkADService, gkRdsGrantService,$scope,$state,$rootScope){
        super($mdDialog, $mdToast, gkADService,$scope,$state,$rootScope);

        vm = this;
        this[GRANT] = gkRdsGrantService;
        this[TOAST] = $mdToast;
        this[DIALOG] = $mdDialog;

        vm.roles = [
                { model: 'readonly', label: 'Read Only', disableFn: vm.disableRoleCheckbox('gk_readonly') },
                { model: 'readonly_confidential', label: 'Read Only (Confidential)', disableFn: vm.disableRoleCheckbox('gk_readonly_confidential') },
                { model: 'datafix', label: 'Datafix', disableFn: vm.disableRoleCheckbox('gk_datafix') },
                { model: 'dba', label: 'DBA', disableFn: vm.disableRoleCheckbox('gk_dba') },
                { model: 'dba_confidential', label: 'DBA (Confidential)', disableFn: vm.disableRoleCheckbox('gk_dba_confidential') }
            ];
        vm.selectedItems = [];
        vm.rdsInstances = [];
    }


    disableRoleCheckbox(roleStr){
        let role = roleStr;
        return () => {
            let dbsThatDontSupportRole = vm.selectedItems.length;
            vm.selectedItems.forEach((item) => {
                if (item.availableRoles.indexOf(role) !== -1) {
                    dbsThatDontSupportRole--;
                }
            });

            return vm.selectedItems.length === 0 || dbsThatDontSupportRole > 0;
        };
    }

    isFormValid(){
        let valueChecked = false;

        if(vm.forms.grantForm && vm.forms.grantForm.selectedRoles) {
            angular.forEach(vm.forms.grantForm.selectedRoles, (v, k) => {
                if (v) {
                    valueChecked = true;
                }
            });
        }

        return vm.usersTable.selected.length === 0 || vm.selectedItems.length === 0 || !valueChecked
    }

    checkIfApprovalNeeded(){
        return vm.forms.grantForm.grantValue > vm.getApprovalBounds() || vm.getApprovalBounds() === -1;
    }

    //Get the lowest threshold value across all selected roles for provided sdlc
    getApprovalBounds(){

        //approvers don't need approval
        if(vm.global.userInfo.role.toLowerCase() === 'approver'){
            return vm.global.rdsMaxDays;
        }

        let approvalRequired = true;
        //first lets make sure they are members of what they are requesting for
        vm.selectedItems.forEach((item) => {
            let resource = vm.global.userInfo.memberships[item.application];
            approvalRequired = !resource || resource.indexOf(vm.forms.awsInstanceForm.selectedAccount.sdlc.toUpperCase()) === -1;
        });

        //if the user isn't part of what he is requesting for warn for approval.
        if(approvalRequired){
            return -1;
        }

        let values = [];

        angular.forEach(vm.forms.grantForm.selectedRoles, (v, k) => {
            if(v){
                values.push(vm.global.userInfo.approvalThreshold[k][vm.forms.awsInstanceForm.selectedAccount.sdlc.toLowerCase()]);
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
                account: vm.forms.awsInstanceForm.selectedAccount,
                region: vm.forms.awsInstanceForm.selectedRegion,
                database: chip
            }
        };
        vm[DIALOG].show(config);
    }

    getMaximumDays(){
        let thresholds = vm.global.rdsOverridePolicy;
        let max = vm.global.rdsMaxDays;
        if(vm.forms.awsInstanceForm && vm.forms.awsInstanceForm.selectedAccount) {
            vm.getSelectedRoles().forEach((role) => {
                if(thresholds[role] && thresholds[role][vm.forms.awsInstanceForm.selectedAccount.sdlc]) {
                    let overrideVal = thresholds[role][vm.forms.awsInstanceForm.selectedAccount.sdlc];
                    if (overrideVal < max) {
                        max = overrideVal;
                    }
                }
            });
        }

        return max;
    }

    getSelectedRoles() {
        let selectedRoles = [];
        angular.forEach(vm.forms.grantForm.selectedRoles, (value, key) => {
            if(value){
                selectedRoles.push(key);
            }
        });

        return selectedRoles;
    }

    grantAccess(){
        let vm = this;
        if(vm.forms.grantForm.$valid) {
            delete vm.error.request;
            let title = 'Confirm Access Request';
            let message = 'This will request access for ' + vm.forms.grantForm.grantValue + ' day(s) for the selected users and instances. ';
            let approvalRequired = vm.checkIfApprovalNeeded();
            if(approvalRequired){
                message += 'This request will require approval.'
            }

            let config = {title:title, message:message, requiresExplanation: approvalRequired};
            vm.spawnTemplatedDialog(config)
                .then((explanation) => {
                    this.fetching.grant = true;
                    let roles = [];
                    vm[GRANT].post(vm.getSelectedRoles(), vm.forms.grantForm.grantValue, vm.usersTable.selected, vm.forms.awsInstanceForm.selectedAccount.alias.toLowerCase(), vm.forms.awsInstanceForm.selectedAccount.sdlc.toLowerCase(), vm.forms.awsInstanceForm.selectedRegion.name, vm.selectedItems, explanation, vm.forms.awsInstanceForm.selectedPlatform)
                        .then((response) => {
                            this.fetching.grant = false;
                            var msg;
                            if(response.data.outcome === 'CREATED') {
                                msg = 'Access was requested for ' + vm.forms.grantForm.grantValue + ' days. If your request required approval,'
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
                                vm.selectedItems.splice(0, vm.selectedItems.length);
                                vm.usersTable.selected = [];
                                vm.selfService = false;
                                delete vm.forms.grantForm.grantValue;

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