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
import RDSDataService from '../../../app/component/shared/RdsUsersDataService';
import AccountDataService from '../../../app/component/shared/AccountDataService';


//Controllers
import md from 'angular-material';
import router from 'angular-ui-router';
import RdsAdminController from '../../../app/component/rds/admin/RdsAdminController';


describe('GateKeeper RDS admin component', function () {

    let $http, $state, scope, controller;

    beforeEach(angular.mock.module(md, router));

    beforeEach(inject(function($rootScope, _$state_, _$http_){
        scope = $rootScope.$new();
        $state =_$state_;
        $http = _$http_;
        $state.current.name = 'gatekeeper.rds.admin';
    }));

    //mock all this stuff out.
    let $q, $rootScope, $httpBackend;
    let gkRdsUserService, gkAccountService;

    describe('RdsAdminController', function(){
        beforeEach(inject(function(_$q_, _$rootScope_, _$httpBackend_, _$http_){
            $q = _$q_;
            $rootScope = _$rootScope_;
            $httpBackend = _$httpBackend_;
            $http = _$http_;
            gkRdsUserService = new RDSDataService($http, $state);
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

            controller = new RdsAdminController(gkRdsUserService, gkAccountService);

            let expected = {
                fetching: false,
                selection: 'multiple',
                // selectionId: 'username',
                toolbar:{
                    header: 'Users',
                    inlineFilter:true,
                    checkboxFilters: [
                        {
                            label: 'Gatekeeper Users Only',
                            filterFn: controller.disableRow
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
               let row = {instanceId:'testid'};
               controller.getUsers(row);
               usersDeferred.resolve(resp);
               scope.$apply();
               expect(gkRdsUserService.search).toHaveBeenCalledWith({
                   account:controller.forms.awsInstanceForm.selectedAccount.alias,
                   region:controller.forms.awsInstanceForm.selectedRegion.name,
                   instanceId:row.instanceId
               });
               expect(controller.usersTable.data).toEqual(resp.data);
           });
        });
    });
});
