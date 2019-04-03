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
const SCOPE = Symbol();
const ROOTSCOPE = Symbol();
const CONFIG = Symbol();
let STATE = Symbol();

//need this to deal with callbacks
let vm;

import GatekeeperSubmissionDialogController from './GatekeeperSubmissionDialogController';

class GatekeeperSelfServiceController {
    constructor($mdDialog, $mdToast, gkADService, $scope,$state,$rootScope, gkEc2ConfigService) {
        vm = this;
        this[AD] = gkADService;
        this[CONFIG] = gkEc2ConfigService;
        this[DIALOG] = $mdDialog;
        this[TOAST] = $mdToast;
        this[SCOPE] = $scope;
        this[STATE] = $state;
        this[ROOTSCOPE] = $rootScope;

        this.global = $rootScope;
        this.selfService = false;
        this.role = vm.global.userInfo.role;
        this.approvalThreshold = vm.global.userInfo.approvalThreshold;
        this.forms = {};


        this.selfServiceUser = {
            name: vm.global.userInfo.user,
            email: vm.global.userInfo.email,
            userId: vm.global.userInfo.userId
        };

        this.usersTable = {
            selection: 'multiple',
            selectionId: 'userId',
            toolbar: {
                header: "Search Result",
                inlineFilter: true
            },
            onSelect: this.isSelectedUserCurrentUser,
            onDeselect: this.isSelectedUserCurrentUser,
            headers: [
                {dataType: 'string', display: 'User ID', value: 'userId'},
                {dataType: 'string', display: 'Name', value: 'name'},
                {dataType: 'string', display: 'E-Mail', value: 'email'}
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
            }
        };

        this.error = {};

        this.fetching = {
            ad: false
        };

        this.confirm = true;
        this[SCOPE].$on('$stateChangeStart', (event, toState, toParams) => {
            vm.global.rollbackIndex = vm.global.selectedIndex;
            this.confirmStateChange(event, toState, toParams);
        });
    }



    confirmStateChange(event,toState,toParams){
        var isPristine = Object.keys(vm.forms).every(function(key){
            return !vm.forms[key] || !vm.forms[key].$dirty;
        });

        if (!isPristine && vm.confirm) {
            event.preventDefault();
            let title  = 'Warning';
            let msg = 'By navigating away from this page, all changes will be lost. Proceed?';
            vm.spawnConfirmDialog(title, msg)
                .then(() => {
                    vm.confirm=false;
                    vm[STATE].go(toState, toParams);
                }).catch(() => {
                    vm[ROOTSCOPE].$broadcast('stateChangeInterrupted');
                });
        }
    }

    isCurrentUserSelected(){
        let selectedIndex = -1;

        this.usersTable.selected.forEach((row, index) => {
            if(row.userId === this.selfServiceUser.userId){
                selectedIndex = index;
            }
        });
        return selectedIndex;
    }

    isSelectedUserCurrentUser(user){
        if(user.userId === vm.selfServiceUser.userId){
            vm.selfService = !vm.selfService;
        }
    }

    toggleSelfService(){
        let selectedIndex = this.isCurrentUserSelected();

        if( selectedIndex !== -1 ) {
            this.selfService = false;
            this.usersTable.selected.splice(selectedIndex, 1);
        }else{
            this.selfService = true;
            let selfServiceUser = this.selfServiceUser;
            //check to see if the current user is in the table...
            this.usersTable.data.forEach((row) => {
                if(row.userId === selfServiceUser.userId){
                    selfServiceUser = row;
                }
            });
            this.usersTable.selected.push(selfServiceUser);
        }

    }

    //AD Component
    searchAD(){
        if(this.forms.adForm.$valid) {
            delete this.error.ad;
            this.fetching.ad = true;
            this.usersTable.data = [];
            this.usersTable.promise = this[AD].search({searchStr:this.forms.adForm.searchText});

            this.usersTable.promise.then((response) => {
                this.usersTable.data = response.data;
                }).catch((error)=>{
                    this.error.ad = error;
                }).finally(() =>{
                    this.fetching.ad = false;
                });
        }
    }


    spawnTemplatedDialog(config){
        let dialog = this[DIALOG].prompt({
                        controller: GatekeeperSubmissionDialogController,
                        controllerAs: 'dialogCtrl',
                        template: require('./template/gatekeeperSubmissionDialog.tpl.html'),
                        parent: angular.element(document.body),
                        locals: config
                    });
        return this[DIALOG].show(dialog);
    }

    spawnConfirmDialog(title, message){
        let confirm = this[DIALOG].confirm()
                .hasBackdrop(true)
                .title(title)
                .content(message)
                .ok('Yes')
                .cancel('No');
        return this[DIALOG].show(confirm);
    }

}

export default GatekeeperSelfServiceController;


