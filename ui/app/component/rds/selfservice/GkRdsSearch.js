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

import Directive from '../../shared/generic/BaseDirective';

/**
 * A simple wrapper around md-data-table
 */

class GkRdsSearch extends Directive{
    constructor(template){
        super(template);
        this.scope = {
            title: '@',
            onSelectFn: '=',
            onDeselectFn: '=',
            selectedItems: '=',
            data: '=',
            forms: '=',
            selection: '@'
        };
        this.transclude = true;
        this.controllerAs="ctrl";
    }

    controller($scope, $mdDialog, gkRDSService, gkAccountService){
        let vm = this;
        vm.rdsService = gkRDSService;
        gkAccountService.fetch().then((response) =>{
            vm.awsAccounts = response.data;
        }).catch(()=>{
            throw new Error('Error fetching accounts');
        });

        vm.error = {};
        vm.forms = $scope.forms;
        vm.rdsInstanceFilter = {
            onlineOnly:false
        };
        vm.awsAccounts = [];
        vm.fetching = false;

        vm.disableRow = (row) => {
            return row.status !== 'available';
        };

        vm.filterOnline = (row) => {
            return row.status === 'available';
        };

        vm.filterOffline = (row) => {
            return !vm.filterOnline(row);
        };

        vm.awsTable = {
            selection: $scope.selection,
            selectionId: 'instanceId',
            toolbar: {
                header: "Available Databases",
                inlineFilter: true,
                checkboxFilters: [
                    {
                        label: "Available Only",
                        filterFn: vm.filterOnline
                    },
                    {
                        label: "Unavailable Only",
                        filterFn: vm.filterOffline
                    }
                ]
            },
            onSelect: $scope.onSelectFn,
            onDeselect: $scope.onDeselectFn,
            headers: [
                {dataType: 'string', display: 'Instance Name', value: 'name'},
                {dataType: 'string', display: 'Database Name', value: 'dbName'},
                {dataType: 'string', display: 'Engine',        value: 'engine'},
                {dataType: 'string', display: 'Available Roles',   value: 'roles'},
                {dataType: 'string', display: 'Status',        value: 'status'}
            ],
            data: $scope.data,
            selected: $scope.selectedItems,
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

        vm.searchRDSInstances = () => {
            if(vm.forms.awsInstanceForm.$valid) {
                delete vm.error.aws;
                vm.fetching = true;
                vm.awsTable.data.splice(0, vm.awsTable.data.length);
                vm.awsTable.promise = vm.rdsService.search(
                    {
                        account: vm.forms.awsInstanceForm.selectedAccount.alias.toLowerCase(),
                        region: vm.forms.awsInstanceForm.selectedRegion.name,
                        searchText:vm.forms.awsInstanceForm.searchText,
                    });

                vm.awsTable.promise.then((response) => {
                    console.info(response);
                    response.data.forEach((item) => {
                        let roles = '';
                        if(item.availableRoles !== null) {
                            item.availableRoles.forEach((role, index) => {
                                roles += role.substr(3).replace('_confidential', ' (c)') + (index < item.availableRoles.length - 1 ? ' | ' : '');
                            });
                        }
                        item.roles = roles;
                    });
                    vm.awsTable.data.push.apply(vm.awsTable.data, response.data);
                }).catch((error) =>{
                    vm.error.aws = error;
                }).finally(() =>{
                    vm.fetching = false;
                });
            }
        };

        /**
         * Called from the UI to clear out the AWS Instances if a change in region or account is made.
         */
        vm.clearInstances = () => {
            let title = 'Change Account/Region/Platform';
            let message = 'Changing Account, Region, or Platform will clear out your currently selected instances. Are you sure?';

            //only need to do anything if the user has anything selected
            if(vm.awsTable.selected.length > 0) {
                vm.dialogOpened = true;
                vm.spawnConfirmDialog(title, message)
                    .then(() => {
                        vm.awsTable.selected.splice(0, vm.awsTable.selected.length);
                        vm.awsTable.data.splice(0, vm.awsTable.data.length);
                        if(vm.forms.awsInstanceForm.selectedAccount !== vm.lastSelectedAccount){
                            delete vm.forms.awsInstanceForm.selectedRegion;
                        }
                        vm.forms.awsInstanceForm.$setUntouched();
                        vm.forms.awsInstanceForm.$setPristine();
                        if($scope.onDeselectFn) {
                            $scope.onDeselectFn();
                        }
                    }).catch(() => {
                    //Do nothing, essentially
                    vm.forms.awsInstanceForm.selectedAccount = vm.lastSelectedAccount;
                    vm.forms.awsInstanceForm.selectedRegion = vm.lastSelectedRegion;
                    vm.forms.awsInstanceForm.selectedPlatform = vm.lastSelectedPlatform;
                }).finally(() => {
                    vm.dialogOpened = false;
                });
            }else{
                vm.lastSelectedAccount = vm.forms.awsInstanceForm.selectedAccount;
                vm.lastSelectedRegion = vm.forms.awsInstanceForm.selectedRegion;
                vm.lastSelectedPlatform = vm.forms.awsInstanceForm.selectedPlatform;
                vm.awsTable.data.splice(0, vm.awsTable.data.length);
                if($scope.onDeselectFn) {
                    $scope.onDeselectFn();
                }
            }
        };

        vm.spawnConfirmDialog = (title, message) =>{
            let confirm = $mdDialog.confirm()
                .hasBackdrop(true)
                .title(title)
                .content(message)
                .ok('Yes')
                .cancel('No');
            return $mdDialog.show(confirm);
        };

    }
}

export default GkRdsSearch;



