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

import GatekeeperController from '../GatekeeperController';

/**
 * The main controller of gatekeeper ec2
 */

let STATE = Symbol();
let ROLES = {
    approver: 'APPROVER',
    auditor: 'AUDITOR',
    developer: 'DEV',
    operations: 'OPS',
    support:'SUPPORT',
    unauthorized: 'UNAUTHORIZED'
};

let STATES = {
    selfService: 'gk.ec2.selfservice',
    requests: 'gk.ec2.requests',
    history: 'gk.ec2.history',
    admin: 'gk.ec2.administration',
    denied: 'gk.denied',
    error: 'gk.error'
};

let LABELS = {
    selfService: 'Request Access',
    requests: 'Access Requests',
    history: 'Access History',
    admin: 'Administration'
};

let vm;
const SCOPE = Symbol();

class GatekeeperEc2Controller extends GatekeeperController{
    constructor($state, gkRoleService, $scope, $rootScope, $stateParams){
        super($state, gkRoleService, $scope, $rootScope);

        this[STATE] = $state;
        this[SCOPE] = $scope;
        vm = this;
        vm.global = $rootScope;
        vm.global.rollbackIndex=-1;
        vm.global.rollbackView= $stateParams.selectedIndex;
        vm.global.selectedIndex= $stateParams.selectedIndex;
        vm.global.selectedView  = $stateParams.selectedView;
        vm.global.tabData = {
            selfService:{
                label: LABELS.selfService,
                enabled: false,
                goToState:STATES.selfService
            },
            requests: {
                label: LABELS.requests,
                enabled: false,
                goToState:STATES.requests
            },
            history: {
                label: LABELS.history,
                enabled: false,
                goToState:STATES.history
            },
            admin: {
                label: LABELS.admin,
                enabled: false,
                goToState:STATES.admin,
                hidden: true
            }
        };

        if(vm.global.selectedIndex !== -1){
            Object.values(vm.global.tabData)[vm.global.selectedIndex].enabled = true;
        }


        gkRoleService.fetch().then((result)=> {
            let vm = this;
            let data = result.data;
            vm.global.userInfo.approvalThreshold = data.approvalThreshold;
            vm.global.userInfo.memberships = data.memberships;
            vm.global.userInfo.userId = data.userId;
            vm.global.userInfo.user = data.name;
            vm.global.userInfo.email = data.email;
            vm.global.ec2MaxHours = data.maxHours;
            vm.global.ec2OverridePolicy = data.overridePolicy;
            vm.global.userInfo.isApprover = data.approver;


            if([ROLES.approver, ROLES.support, ROLES.auditor].indexOf(data.role) === -1 && data.memberships.length === 0 ){
                vm.global.userInfo.role = ROLES.unauthorized;
            }else{
                vm.global.userInfo.role = data.role;
            }

            switch (vm.global.userInfo.role) {
                case ROLES.approver:
                case ROLES.auditor:
                    if(vm.global.selectedIndex === -1) {
                        vm.global.tabData.requests.enabled = true;
                        vm.global.selectedIndex = findKeyIndex(vm.global.tabData, 'selfService');
                    }
                    break;
                case ROLES.developer:
                case ROLES.operations:
                case ROLES.support:
                    if(vm.global.selectedIndex === -1) {
                        vm.global.tabData.selfService.enabled = true;
                        vm.global.selectedIndex = findKeyIndex(vm.global.tabData, 'requests');
                    }
                    break;
                default:
                    vm[STATE].go(STATES.denied);
            }
        }).catch((result) => {
            this.error = result;
            vm[STATE].go(STATES.error);
        });


        //Overwrite the rollback index for when
        //stateChangeInterrupted is sent out
        this[SCOPE].$on('$stateChangeSuccess',() =>
        {
            vm.global.rollbackIndex=vm.global.selectedIndex;
        });

        //Listen for the stateChangeInterrupted event
        //that is broadcast from the self-service controller
        //when a user clicks 'cancel' after dirty check
        this[SCOPE].$on('stateChangeInterrupted', () =>
        {
            vm.global.selectedIndex=vm.global.rollbackIndex;
        });

        //private helper function to get the 'index' of
        //an object in the tabData so that the selectedIndex
        //can be assigned a 'default' state based on role
        let findKeyIndex = function(jsonObject, wantedKey){
            var counter=0;
            Object.keys(jsonObject).forEach(function(key){
                if(key===wantedKey){
                    return counter;
                }
                counter++;
            })
        }

    }
}

export default GatekeeperEc2Controller;



