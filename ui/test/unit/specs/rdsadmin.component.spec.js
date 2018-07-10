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

//Dataservices
import RdsRevokeUsersDataService from '../../../app/component/shared/RdsRevokeUsersDataService';
import RdsUsersDataService from '../../../app/component/shared/RdsUsersDataService';
import AccountDataService from '../../../app/component/shared/AccountDataService';

//Controllers
import md from 'angular-material';
import router from 'angular-ui-router';
import RdsAdminController from '../../../app/component/rds/admin/RdsAdminController';


describe('GateKeeper RDS admin component', function () {

    let $http, $state, $mdDialog, $mdToast, scope, controller;

    beforeEach(angular.mock.module(md, router));

    beforeEach(inject(function($rootScope, _$state_, _$http_,_$mdDialog_,_$mdToast_){
        scope = $rootScope.$new();
        $state =_$state_;
        $http = _$http_;
        $mdDialog = _$mdDialog_;
        $mdToast = _$mdToast_;
        $state.current.name = 'gatekeeper.rds.admin';
    }));

    //mock all this stuff out.
    let $q, $rootScope, $httpBackend;
    let gkRdsUserService, gkAccountService, gkRdsRevokeUsersService;

    describe('RdsAdminController', function(){
        beforeEach(inject(function(_$q_, _$rootScope_, _$httpBackend_){
            $q = _$q_;
            $rootScope = _$rootScope_;
            $httpBackend = _$httpBackend_;

            gkRdsRevokeUsersService = new RdsRevokeUsersDataService($http, $state);
            gkRdsUserService = new RdsUsersDataService($http, $state);
            gkAccountService = new AccountDataService($http, $state);

        }));

        let testInit = (happy) => {
            let deferred = $q.defer();
            spyOn(gkAccountService, 'fetch').and.returnValue(deferred.promise);
            let resp = {data:['stuff']};

            $rootScope.userInfo = {
                role: 'APPROVER',
                userId:'testId',
                user:'test',
                email:'test@email.com'
            };


            if(happy) {
                deferred.resolve(resp);
            }else{
				
                deferred.reject(resp);
            }

            controller = new RdsAdminController($mdDialog, $mdToast, gkRdsUserService, gkRdsRevokeUsersService, gkAccountService);

            let expected = {
                fetching: false,
                selection: 'multiple',
                // selectionId: 'username',
                toolbar:{
                    header: '',
                    inlineFilter:true,
                    checkboxFilters: [
                        {
                            label: 'GK Users',
                            filterFn: controller.filterGk
                        }
                    ]
                },
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
                disableRow: controller.disableRow,
                disableBackgroundColor: 'rgba(0,0,0,0.12)'
            };

            expect(controller.usersTable).toEqual(expected);
        };

        it('Should Initialize properly', function(){
            testInit();
        });

        describe('Test disableRow method', function(){
            it('should return true if the user does not begin with gk_', function(){
                let row = {
                    username: 'notaproperguy'
                };
                expect(controller.disableRow(row)).toBeTruthy();
            });

            it('should return false if the user does not begin with gk_', function(){
                let row = {
                    username: 'gk_thishouldwork'
                };
                expect(controller.disableRow(row)).toBeFalsy();
            });
        });
        describe('Test getUsers method', function () {
            let usersDeferred;
            beforeEach(() => {
                usersDeferred = $q.defer();
                spyOn(gkRdsUserService, 'search').and.returnValue(usersDeferred.promise);
            });
            it(' should return users', function(){
               let resp = {data:[{username:'testuser'},{username:'another'}]};
               testInit(true);
               controller.forms.awsInstanceForm = {
                   selectedAccount: { alias:'ut'},
                   selectedRegion: { name:'us-east-1'}
               };
               let row = {name:'testid'};
               controller.getUsers(row);
               usersDeferred.resolve(resp);
               scope.$apply();
               expect(gkRdsUserService.search).toHaveBeenCalledWith({
                   account:controller.forms.awsInstanceForm.selectedAccount.alias,
                   region:controller.forms.awsInstanceForm.selectedRegion.name,
                   instanceName:row.name
               });
               expect(controller.usersTable.data).toEqual(resp.data);
           });
        });

        describe('Test getRawUsers method', function() {
           it('Should show the raw users in an alert dialog', function() {
               testInit(true);
               let deferred = $q.defer();
               spyOn($mdDialog, 'show').and.returnValue(deferred.promise);
               controller.forms.awsInstanceForm = {
                   selectedAccount: { alias:'ut'},
                   selectedRegion: { name:'us-east-1'}
               };

               let selectedDb = {name:'mytest'};
               controller.selectedItems = [selectedDb];

               controller.usersTable.data = [
                   {username:'gk_test1'},
                   {username:'a_test1'},
               ];

               spyOn(controller, 'spawnAlertDialog')

               controller.showRawUsers();
               expect(controller.spawnAlertDialog).toHaveBeenCalledWith('Users for ' + selectedDb.name, ['a_test1', 'gk_test1']);
           });
        });

        describe('Test revokeUsers method', function() {
            it('Should update users on success', function() {
                testInit(true);
                let deferred = $q.defer();
                let revokeDefer = $q.defer();

                spyOn(controller, 'spawnConfirmDialog').and.returnValue(deferred.promise);
                spyOn(gkRdsRevokeUsersService, 'delete').and.returnValue(revokeDefer.promise);
                spyOn($mdToast, 'show');

                controller.forms.awsInstanceForm = {
                    selectedAccount: { alias:'ut'},
                    selectedRegion: { name:'us-east-1'}
                };

                let selectedDb = {name:'mytest'};
                controller.selectedItems = [selectedDb];

                let user1 = {username:'gk_test1'};
                let user2 = {username:'a_test1'};

                controller.usersTable.data = [
                    user1,
                    user2
                ];

                controller.usersTable.selected = [
                    user1
                ];

                controller.revokeUsersFromDb();

                expect(controller.spawnConfirmDialog).toHaveBeenCalledWith('Revoke User Access', 'This will delete the users you have selected, are you sure?');

                deferred.resolve();
                revokeDefer.resolve({data:[user2]});

                $rootScope.$apply();

                expect($mdToast.show).toHaveBeenCalled();
                expect(controller.usersTable.data).toEqual([user2]);
            });

            it('Should set an error message on failure', function() {
                testInit(true);
                let deferred = $q.defer();
                let revokeDefer = $q.defer();

                spyOn(controller, 'spawnConfirmDialog').and.returnValue(deferred.promise);
                spyOn(gkRdsRevokeUsersService, 'delete').and.returnValue(revokeDefer.promise);
                spyOn($mdToast, 'show');

                controller.forms.awsInstanceForm = {
                    selectedAccount: { alias:'ut'},
                    selectedRegion: { name:'us-east-1'}
                };

                let selectedDb = {name:'mytest'};
                controller.selectedItems = [selectedDb];

                let user1 = {username:'gk_test1'};
                let user2 = {username:'a_test1'};

                controller.usersTable.data = [
                    user1,
                    user2
                ];

                controller.usersTable.selected = [
                    user1
                ];

                controller.revokeUsersFromDb();

                expect(controller.spawnConfirmDialog).toHaveBeenCalledWith('Revoke User Access', 'This will delete the users you have selected, are you sure?');

                let errorStr = 'This is a Test';
                deferred.resolve();
                revokeDefer.reject({data:{error:errorStr}});

                $rootScope.$apply();

                expect($mdToast.show).not.toHaveBeenCalled();
                expect(controller.usersTable.data).toEqual([user1,user2]);
                expect(controller.error.users = errorStr);
            });
        });

    });
});
