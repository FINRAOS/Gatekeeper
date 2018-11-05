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

import GatekeeperAdminController from '../../shared/admin/GatekeeperAdminController';
import _ from 'lodash';

const USERS = Symbol();
const REVOKE = Symbol();
const TOAST = Symbol();
let vm;

class RdsAdminController extends GatekeeperAdminController{
    constructor($mdDialog, $mdToast, gkRdsUsersService, gkRdsRevokeUsersService, gkAccountService){
        super($mdDialog, $mdToast);
        vm = this;
        vm[USERS] = gkRdsUsersService;
        vm[REVOKE] = gkRdsRevokeUsersService;
        vm[TOAST] = $mdToast;

        vm.selectedItems = [];
        vm.rdsInstances = [];

        gkAccountService.fetch().then((response) =>{
            this.awsAccounts = response.data;
        }).catch(()=>{
            throw new Error('Error fetching accounts');
        });

        vm.rdsInstanceFilter = {
            onlineOnly:false
        };

        vm.awsAccounts = [];

        vm.usersTable = {
            fetching: false,
            selection: 'multiple',
            // selectionId: 'userId',
            toolbar: {
                header: '',
                inlineFilter: true,
                checkboxFilters: [
                    {
                        label: 'GK Users',
                        filterFn: vm.filterGk
                    }]
            },
            // onSelect: $scope.onSelectFn,
            // onDeselect: $scope.onDeselectFn,
            headers: [
                {dataType: 'string', display: 'User Name', value: 'username'},
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
            disableRow: vm.disableRow,
            disableBackgroundColor: 'rgba(0,0,0,0.12)'
        };

    }

    //also used for filtering.
    disableRow(row){
        return !row.username.toLowerCase().startsWith('gk_');
    }

    filterGk(row){
        return !vm.disableRow(row);
    }

    getUsers(row){
        delete vm.error.users;
        vm.usersTable.fetching = true;
        vm.usersTable.data.splice(0, vm.usersTable.data.length);
        vm.usersTable.promise = vm[USERS].search(
            {
                account: vm.forms.awsInstanceForm.selectedAccount.alias.toLowerCase(),
                region: vm.forms.awsInstanceForm.selectedRegion.name,
                instanceName: row.name,
            });

        vm.usersTable.promise.then((response) => {
            vm.usersTable.data.push.apply(vm.usersTable.data, response.data);
        }).catch((error) =>{
            vm.error.users = error;
        }).finally(() =>{
            vm.usersTable.fetching = false;
        });
    }

    revokeUsersFromDb(){
        let title = "Revoke User Access";
        let message = "This will delete the users you have selected, are you sure?";
        vm.spawnConfirmDialog(title, message)
            .then(() => {
                delete vm.error.users;
                vm.blocking = true;
                vm.usersTable.promise = vm[REVOKE].delete(vm.forms.awsInstanceForm.selectedAccount.alias.toLowerCase(),
                    vm.forms.awsInstanceForm.selectedRegion.name,
                    vm.selectedItems[0].name,
                    vm.usersTable.selected);

                vm.usersTable.promise.then((response) => {
                    vm.usersTable.data = response.data;
                    vm[TOAST].show(
                        vm[TOAST].simple()
                            .textContent('Users have been successfully revoked!')
                            .position('bottom right')
                            .hideDelay(10000));
                }).catch((error) => {
                    vm.error.users = error.data.message;
                }).finally(() => {
                    vm.usersTable.selected = [];
                    vm.blocking = false;
                });
            });
    }

    showRawUsers(){
        let title = 'Users for ' + vm.selectedItems[0].name;
        let message = _.orderBy(
                _.map(vm.usersTable.data, (item) => {
            return item.username;
            }), (item) => { return item.toLowerCase(); }, ['asc']);
        vm.spawnAlertDialog(title, message);
    }
}


export default RdsAdminController;



