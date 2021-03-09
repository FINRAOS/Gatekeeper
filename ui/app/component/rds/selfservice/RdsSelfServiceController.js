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
const CONFIG = Symbol();

//need this to deal with callbacks
let vm;

import GatekeeperSelfServiceController from '../../shared/selfservice/GatekeeperSelfServiceController';
import GatekeeperJustificationConfig from '../../shared/selfservice/model/GatekeeperJustificationConfig';

class RdsSelfServiceController extends GatekeeperSelfServiceController {
    constructor($mdDialog, $mdToast, gkADService, gkRdsGrantService, gkRdsConfigService,$scope,$state,$rootScope){
        super($mdDialog, $mdToast, gkADService,$scope,$state,$rootScope);

        vm = this;
        this[GRANT] = gkRdsGrantService;
        this[CONFIG] = gkRdsConfigService;
        this[TOAST] = $mdToast;
        this[DIALOG] = $mdDialog;

        vm.roles = [
                { model: 'readonly', label: 'Read Only', disableFn: vm.disableRoleCheckbox('gk_readonly'),roleGroup: vm.returnRoleGroup('gk_readonly')},
                { model: 'readonly_confidential', label: 'Read Only (Confidential)', disableFn: vm.disableRoleCheckbox('gk_readonly_confidential'),roleGroup: vm.returnRoleGroup('gk_readonly_confidential')},
                { model: 'datafix', label: 'Datafix', disableFn: vm.disableRoleCheckbox('gk_datafix'),roleGroup: vm.returnRoleGroup('gk_datafix')},
                { model: 'dba', label: 'DBA', disableFn: vm.disableRoleCheckbox('gk_dba'),roleGroup: vm.returnRoleGroup('gk_dba') },
                { model: 'dba_confidential', label: 'DBA (Confidential)', disableFn: vm.disableRoleCheckbox('gk_dba_confidential'),roleGroup: vm.returnRoleGroup('gk_dba_confidential') }
            ];
        vm.selectedItems = [];
        vm.rdsInstances = [];

        this[CONFIG].fetch().then((response) =>{
            let data = response.data;
            vm.ticketIdFieldMessage = data.ticketIdFieldMessage;
            vm.ticketIdFieldRequired = data.ticketIdFieldRequired;
            vm.explanationFieldRequired = data.explanationFieldRequired;
        }).catch((error)=>{
            console.log('Error retrieving justification config: ' + error);
        });


    }
    returnRoleGroup(role){
        let map;
        if(vm.global.userInfo.rdsApplicationRoles !== undefined) {
            map = new Map(Object.entries(vm.global.userInfo.rdsApplicationRoles));
        }
        return () => {
            let application = null;
            let applicationRoles = null;
            let text = '';
            vm.selectedItems.forEach((item) => {
                application = item.application;
                applicationRoles = item.applicationRoles;
                if (item.availableRoles.indexOf(role) === -1) {
                    text = 'This role does not exist for the current database';
                }
                });
            if(!vm.global.userInfo.isApprover) {
                if (map !== undefined) {
                    if (map.get(application) !== undefined) {
                        let roleName = this.convertRoleText(role);
                        applicationRoles.forEach((roleItem) => {
                            if (roleItem.gkRole === roleName) {
                                if(text ===''){
                                    text = 'User requires the following role: ' + roleItem.name;
                                }
                            }
                        });
                    }
                }
            }
            if(text === ''){
                text = 'This role is currently disabled';
            }
            return text;
        };

    }
    convertRoleText(role){
        switch (role){
            case 'gk_datafix':
                return 'DF';
            case 'gk_dba_confidential':
                return 'DBAC';
            case 'gk_readonly':
                return 'RO';
            case 'gk_dba':
                return 'DBA';
            case 'gk_readonly_confidential':
                return 'ROC';
            default:
                return role;

        }
    }
    shallowEqual(userGroup, rdsGroup){
        if(userGroup.gkRole !== rdsGroup.gkRole){
            return false;
        }
        if(userGroup.application !== rdsGroup.application){
            return false;
        }
        if(userGroup.sdlc !== rdsGroup.sdlc){
            return false;
        }
        if(userGroup.name !== rdsGroup.name){
            return false;
        }
        return true;
    }
    disableRoleCheckbox(roleStr){
        let role = roleStr;
        let map;
        if(vm.global.userInfo.rdsApplicationRoles !== undefined) {
            map = new Map(Object.entries(vm.global.userInfo.rdsApplicationRoles));
        }
        return () => {
            let dbsThatDontSupportRole = vm.selectedItems.length;
            let application = null;
            let applicationRoles = null;

            vm.selectedItems.forEach((item) => {
                if (item.availableRoles.indexOf(role) !== -1) {
                    dbsThatDontSupportRole--;
                }
                application = item.application;
                applicationRoles = item.applicationRoles;

            });
            if(!vm.global.userInfo.isApprover) {
                if (map !== undefined) {

                    if (map.get(application) !== undefined) {
                        let roleName = this.convertRoleText(role);
                        let roleObject;
                        applicationRoles.forEach((roleItem) => {
                            if (roleItem.gkRole === roleName) {
                                roleObject = roleItem;
                            }
                        });

                        if (!map.get(application).some(mapRole => this.shallowEqual(mapRole, roleObject))) {
                            return true;
                        }
                    } else if (applicationRoles !== null) {
                        if (applicationRoles.length > 0) {
                            return true;
                        }
                    }
                }
            }
            return vm.selectedItems.length === 0 || dbsThatDontSupportRole > 0;
        };
    }

    disableAddOtherUser() {
        if(!vm.global.userInfo.isApprover){
            let items = vm.selectedItems[0];
            if(items !== undefined) {
                let applicationRoles = items.applicationRoles;
                if(applicationRoles !== null){
                    if (applicationRoles.length > 0) {
                        vm.restrictedRDSApplication = true;
                        while (this.usersTable.selected.length > 0) {
                            this.usersTable.selected.pop();
                        }
                        this.usersTable.selected.push(this.selfServiceUser);
                        vm.selfService = true;
                    }
                    else{
                        vm.restrictedRDSApplication = false;
                    }
                }
                else{
                    vm.restrictedRDSApplication = false;
                }
            }
            else{
                vm.restrictedRDSApplication = false;
            }
        }
        else {
            vm.restrictedRDSApplication= false;
        }
    }

    isFormValid(){
        let valueChecked = false;
        this.disableAddOtherUser();

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
        if(vm.global.userInfo.isApprover){
            return vm.global.rdsMaxDays;
        }

        let approvalRequired = true;
        //first lets make sure they are members of what they are requesting for
        vm.selectedItems.forEach((item) => {
            let resource = vm.global.userInfo.roleMemberships;
            approvalRequired = !resource || !resource[item.application];
        });

        //if the user isn't part of what he is requesting for warn for approval.
        if (approvalRequired) {
            return -1;
        }

        let values = [];

        angular.forEach(vm.forms.grantForm.selectedRoles, (v, k) => {
            vm.selectedItems.forEach( (item) => {
                if (v) {
                    let appApprovalThreshold = vm.global.userInfo.approvalThreshold[item.application]['appApprovalThresholds'][k.toUpperCase()][vm.forms.awsInstanceForm.selectedAccount.sdlc.toLowerCase()];
                    values.push(appApprovalThreshold);
                }
            });
        });

        let approvalThreshold = Math.min.apply(Math, values);
        let maxDays = this.getMaximumDays()

        if(approvalThreshold <= maxDays) {
            return approvalThreshold;
        } else {
            return maxDays;
        }

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
        let overrideExists = false;
        if(vm.forms.awsInstanceForm && vm.forms.awsInstanceForm.selectedAccount) {
            vm.getSelectedRoles().forEach((role) => {
                if(thresholds[role] && thresholds[role][vm.forms.awsInstanceForm.selectedAccount.sdlc]) {
                    let overrideVal = thresholds[role][vm.forms.awsInstanceForm.selectedAccount.sdlc];
                    if(overrideExists === false) {
                        overrideExists = true;
                        max = overrideVal;
                    }
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

            let config = {title:title, message:message, requiresExplanation: approvalRequired, justificationConfig:new GatekeeperJustificationConfig(vm.ticketIdFieldMessage, vm.ticketIdFieldRequired, vm.explanationFieldRequired)};
            vm.spawnTemplatedDialog(config)
                .then((justification) => {
                    this.fetching.grant = true;
                    let roles = [];
                    vm[GRANT].post(vm.getSelectedRoles(), vm.forms.grantForm.grantValue, vm.usersTable.selected, vm.forms.awsInstanceForm.selectedAccount.alias.toLowerCase(), vm.forms.awsInstanceForm.selectedAccount.sdlc.toLowerCase(), vm.forms.awsInstanceForm.selectedRegion.name, vm.selectedItems, justification.ticketId, justification.explanation, vm.forms.awsInstanceForm.selectedPlatform)
                        .then((response) => {
                            this.fetching.grant = false;
                            var msg;
                            if(response.data.outcome === 'CREATED') {
                                msg = 'Access was requested for ' + vm.forms.grantForm.grantValue + ' days. If your request required approval,'
                                    + ' access will not be granted until your request is reviewed and actioned by an approver. Once granted, users will' +
                                    ' be sent an email with further instructions';
                                vm[TOAST].show(
                                    vm[TOAST].simple()
                                        .textContent(msg)
                                        .position('bottom right')
                                        .hideDelay(10000)
                                );
                            }else if(response.data.outcome === 'USER_NOT_AUTHORIZED'){
                                msg = 'User is not authorized to make this request. \n' + response.data.response;
                                let config = {
                                    clickOutsideToClose: true,
                                    title: 'ERROR',
                                    template: require("../../shared/request/template/error.tpl.html"),
                                    parent: angular.element(document.body),
                                    locals: {
                                        message: msg
                                    },
                                    controller: ['$scope', '$mdDialog', 'message', function ($scope, $mdDialog, message) {
                                        $scope.message = message;
                                        $scope.cancel = function () {
                                            $mdDialog.cancel();
                                        };
                                    }
                                    ]
                                };
                                vm[DIALOG].show(config);
                            }
                        else{
                                msg = 'Access was NOT requested due to old/expired users having temporary tables on the databases that were selected, please work with the Ops team to get these users cleaned up.';
                                let config = {
                                    clickOutsideToClose: true,
                                    title: 'ERROR',
                                    template: require("../../shared/request/template/error.tpl.html"),
                                    parent: angular.element(document.body),
                                    locals: {
                                        message: msg
                                    },
                                    controller: ['$scope', '$mdDialog', 'message', function ($scope, $mdDialog, message) {
                                        $scope.message = message;
                                        $scope.cancel = function () {
                                            $mdDialog.cancel();
                                        };
                                    }
                                    ]
                                };
                                vm[DIALOG].show(config);
                            }

                            if(response.data.outcome === 'CREATED') {
                                //deselect all users and instances
                                vm.selectedItems.splice(0, vm.selectedItems.length);
                                vm.usersTable.selected = [];
                                vm.selfService = false;
                                delete vm.forms.grantForm.grantValue;

                                //needed since forms are still dirty
                                vm.confirm = false;
                            }else if(response.data.outcome === 'USER_NOT_AUTHORIZED'){
                                vm.error.roles = response.data.response;
                            }
                            else{
                                vm.error.databases = response.data.response;
                            }
                        }).catch((error) => {
                            this.fetching.grant = false;
                            let msg = 'There was an error while trying to request access. Please make sure the requested instances are in a running state and are able to access AWS APIs.';
                            vm.error.request = error;
                            let config = {
                                clickOutsideToClose: true,
                                title: 'ERROR',
                                template: require("../../shared/request/template/error.tpl.html"),
                                parent: angular.element(document.body),
                                locals: {
                                    message: msg
                                },
                                controller: ['$scope', '$mdDialog', 'message', function ($scope, $mdDialog, message) {
                                    $scope.message = message;
                                    $scope.cancel = function () {
                                        $mdDialog.cancel();
                                    };
                                }
                                ]
                            };
                            vm[DIALOG].show(config);
                    });
            });
        }
    }
}

export default RdsSelfServiceController;
