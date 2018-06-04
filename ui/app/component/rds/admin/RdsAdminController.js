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

const USERS = Symbol();
let vm;

class RdsAdminController extends GatekeeperAdminController{
    constructor(gkRdsUsersService, gkAccountService){
        super();
        vm = this;
        vm[USERS] = gkRdsUsersService;

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
                header: 'Users',
                inlineFilter: true,
                checkboxFilters: [
                    {
                        label: 'Gatekeeper Users Only',
                        filterFn: vm.disableRow
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
        return !row.username.startsWith('gk_');
    }

    getUsers(row){
        delete vm.error.users;
        vm.usersTable.fetching = true;
        vm.usersTable.data.splice(0, vm.usersTable.data.length);
        vm.usersTable.promise = vm[USERS].search(
            {
                account: vm.forms.awsInstanceForm.selectedAccount.alias.toLowerCase(),
                region: vm.forms.awsInstanceForm.selectedRegion.name,
                instanceId: row.instanceId,
            });

        vm.usersTable.promise.then((response) => {
            vm.usersTable.data.push.apply(vm.usersTable.data, response.data);
        }).catch((error) =>{
            vm.error.users = error;
        }).finally(() =>{
            vm.usersTable.fetching = false;
        });
    }
}


export default RdsAdminController;



