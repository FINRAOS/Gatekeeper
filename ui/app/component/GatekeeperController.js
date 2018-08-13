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

/**
 * The main controller of gatekeeper
 */

let STATE = Symbol();

let STATES = {
    ec2: 'gk.ec2',
    rds: 'gk.rds',
    denied: 'gk.denied',
    error: 'gk.error'
};

let LABELS = {
    ec2: 'EC2',
    rds: 'RDS',
};

let vm;
const SCOPE = Symbol();
class GatekeeperController{
    constructor($state, gkRoleService, $scope, $rootScope, $stateParams){
        require('../css/main.css');
        require('../../node_modules/hover.css/css/hover-min.css');

        this.global = $rootScope;
        this[STATE] = $state;
        this[SCOPE]=$scope;
        this.rollbackIndex=-1;
        this.states = STATES;
        vm = this;

        if(!vm.global.userInfo) {
            vm.global.userInfo = {};
        }

        this.buttons = {
            ec2:{
                label: LABELS.ec2,
                enabled: false,
                goToState:STATES.ec2

            },
            rds: {
                label: LABELS.rds,
                enabled: false,
                goToState:STATES.rds
            },
        };

        //Overwrite the rollback index for when
        //stateChangeInterrupted is sent out
        this[SCOPE].$on('$stateChangeSuccess',() =>
        {
            vm.global.rollbackView=vm.global.selectedView;
            vm.global.rollbackIndex=vm.global.selectedIndex;
        });

        //Listen for the stateChangeInterrupted event
        //that is broadcast from the self-service controller
        //when a user clicks 'cancel' after dirty check
        this[SCOPE].$on('stateChangeInterrupted', () =>
        {
            vm.global.selectedView = vm.global.rollbackView;
            vm.global.selectedIndex = vm.global.rollbackIndex;
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
        };

        //grab a user for the toolbar at the top
        gkRoleService.fetch().then((result)=> {
            let vm = this;
            let data = result.data;
            vm.global.userInfo.userId = data.userId;
            vm.global.userInfo.user = data.name;
            vm.global.userInfo.role = data.role;
            vm.global.userInfo.email = data.email;
        }).catch((result) => {
            this.error = result;
            this[STATE].go(STATES.error)
        });

    }

    ready(){
        return this.global.userInfo.user && this.global.userInfo.role !== 'UNAUTHORIZED';
    }

}

export default GatekeeperController;



