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
import AccountDataService from '../../../app/component/shared/AccountDataService';
import AWSDataService from '../../../app/component/shared/AWSDataService';
import ADDataService from '../../../app/component/shared/ADDataService';
import GrantDataService from '../../../app/component/shared/GrantDataService';
import RoleDataService from '../../../app/component/shared/RoleDataService';
import Ec2ConfigService from "../../../app/component/shared/Ec2ConfigService";

//Controllers
import md from 'angular-material';
import GatekeeperSelfServiceController from '../../../app/component/shared/selfservice/GatekeeperSelfServiceController';
import Ec2SelfServiceController from "../../../app/component/ec2/selfservice/Ec2SelfServiceController";


describe('GateKeeper UI self service component', function () {

    let $http, scope, controller;

    beforeEach(angular.mock.module(md));

    beforeEach(inject(function($rootScope){
        scope = $rootScope.$new();
    }));

    //mock all this stuff out.
    let $q, $httpBackend, $mdDialog,$mdToast,gkADService,gkAWSService,gkGrantService, gkAccountService, gkEc2ConfigService, $scope,$state, $rootScope;

    describe('GatekeeperSelfServiceController', function(){
        beforeEach(inject(function(_$mdDialog_, _$mdToast_, $http, _$q_, _$httpBackend_,_$rootScope_){
            $mdDialog = _$mdDialog_;
            $mdToast = _$mdToast_;
            gkAWSService = new AWSDataService($http);
            gkADService  = new ADDataService($http);
            gkGrantService = new GrantDataService($http);
            gkAccountService = new AccountDataService($http);
            gkEc2ConfigService = new Ec2ConfigService($http);
            $q = _$q_;
            $httpBackend = _$httpBackend_;
            $rootScope=_$rootScope_;
            $scope=$rootScope.$new();
            $state = {go:function(){}};

        }));

        let testInit = (happy) => {
            let deferred = $q.defer();
            spyOn(gkAccountService, 'fetch').and.returnValue(deferred.promise);
            let resp = {data:['stuff']};

            $rootScope.userInfo = {
                role: "SUPPORT",
                userId:"testId",
                user:"test",
                email:"test@email.com"
            };


            if(happy) {
                deferred.resolve(resp);
            }else{
				
                deferred.reject(resp);
            }

            controller = new GatekeeperSelfServiceController($mdDialog, $mdToast, gkADService,$scope,$state,$rootScope, gkEc2ConfigService);

            let expected = {
                selection: 'multiple',
                selectionId: 'userId',
                toolbar:{
                    header: "Search Result",
                    inlineFilter:true
                },
                onSelect: controller.usersTable.onSelect,
                onDeselect: controller.usersTable.onDeselect,
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

            expect(controller.usersTable).toEqual(expected);

            expect(controller.selfService).toBeFalsy();
            expect(controller.selfServiceUser).toEqual({userId: 'testId', name:'test', email:'test@email.com'});
        };

        let searchAD = (happy) => {
            testInit(true);

            let deferred = $q.defer();
            spyOn(gkADService, 'search').and.returnValue(deferred.promise);
            let response = {data:[{user:'abc', user:'123', email:'name@org'}]};
            happy ? deferred.resolve(response) : deferred.reject(response);

            controller.forms.adForm = {
                $valid: true,
                searchText:'steve'
            };

            expect(controller.usersTable.data).toEqual([]);

            controller.searchAD();

            expect(gkADService.search).toHaveBeenCalledWith({
                searchStr: controller.forms.adForm.searchText
            });

            let error = '';
            try {
                scope.$apply();
            }catch(e){
                error = e;
            }
            if(happy) {
                expect(controller.usersTable.data).toEqual(response.data);
            }else {
                expect(controller.usersTable.data).toEqual([]);
            }
        };


        it('Should Initialize properly', function(){
            testInit();
        });

        //honestly I have no idea why I need to do this... this code shouldnt even get called.
        it('Should Fetch Users', function(){
            $httpBackend.expectGET('/api/gatekeeper/grantAccess').respond({object: "hello"});
            searchAD(true);
        });

        it('Should Fetch Users - with error', function(){
            $httpBackend.expectGET('/api/gatekeeper/grantAccess').respond({object: "hello"});
            searchAD(false);
        });

    });

    describe('GatekeeperSelfServiceController', function(){
        let state;
        beforeEach(inject(function(_$mdDialog_, _$mdToast_, $http, _$q_, _$rootScope_){
            $mdDialog = _$mdDialog_;
            $mdToast = _$mdToast_;
            gkADService = new ADDataService($http);
            gkAWSService = new AWSDataService($http);
            gkGrantService = new GrantDataService($http);
            gkAccountService = new AccountDataService($http);
            gkEc2ConfigService = new Ec2ConfigService($http);
            $q = _$q_;
            state = {
                current: {
                    name: 'gatekeeper.ec2'
                }
            };
            if($rootScope === undefined) {
                $rootScope = _$rootScope_;
            }
            if($scope === undefined) {
                $scope = $rootScope.$new();
            }
        }));

        let testInit = (happy) => {
            let deferred = $q.defer();
            let deferredEc2 = $q.defer();
            spyOn(gkAccountService, 'fetch').and.returnValue(deferred.promise);
            spyOn(gkEc2ConfigService, 'fetch').and.returnValue(deferredEc2.promise);
            let resp = {data:['stuff']};
            let respEc2 = {
                data: {
                    ticketIdFieldMessage: 'Please enter a ticket ID: ',
                    ticketIdFieldRequired: true,
                    explanationFieldRequired: true
                }
            };

            $rootScope.userInfo = {
                userId:"testId",
                user:"test",
                role:"approver",
                email:"test@email.com"
            };

            if(happy) {
                deferred.resolve(resp);
                deferredEc2.resolve(respEc2);
            }else{
                deferred.reject(resp);
                deferredEc2.reject(respEc2);
            }

            controller = new Ec2SelfServiceController($mdDialog,
                $mdToast, gkADService, gkAWSService, gkGrantService, gkAccountService, gkEc2ConfigService, $scope,state,$rootScope);


            let failMsg;

            try {
                scope.$apply();
            }catch(e){
                failMsg = e;
            }
            controller.forms.awsInstanceForm = {};

            expect(controller.awsTable).toEqual({
                selection:'multiple',
                selectionId: 'instanceId',
                toolbar:{
                    header:"Search Result",
                    inlineFilter:true,
                    checkboxFilters: [ { label: 'SSM Online Only', filterFn: controller.filterOnline }, { label: 'Show Offline Only', filterFn: controller.filterOffline } ]
                },
                onSelect: controller.checkIfApprovalNeeded,
                onDeselect: controller.checkIfApprovalNeeded,
                headers:[
                    {dataType:'string', display:'Instance ID', value:'instanceId'},
                    {dataType:'string', display:'Instance Name', value:'name'},
                    {dataType:'string', display:'Application', value:'application'},
                    {dataType:'string', display:'Platform', value:'platform'},
                    {dataType:'string', display:'Instance IP', value:'ip'},
                    {dataType:'string', display:'SSM Status', value:'ssmStatus'}
                ],
                data: [],
                selected: [],
                query:{
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
            });

            expect(controller.awsSearchableTags).toEqual(['Instance ID', 'Name', 'IP', 'Application']);
            //aws stuff
            if(happy) {
                expect(controller.awsAccounts).toEqual(resp.data);
            }else{
                expect(failMsg.toString()).toEqual('Error: Error fetching accounts');
                expect(controller.awsAccounts).toEqual([]);
            }
        };

        let searchAWSInstances = (happy) => {
            testInit(true);

            let deferred = $q.defer();
            spyOn(gkAWSService, 'search').and.returnValue(deferred.promise);
            let response = {data:[{id:'123', name:'name', application:'TEST', ip:'127.0.0.1', platform: 'Linux'}]};
            happy ? deferred.resolve(response) : deferred.reject(response);

            controller.forms.awsInstanceForm = {
                $valid: true,
                selectedAccount:{sdlc:'DEV', alias:'DEV'},
                selectedRegion:{name:'US-EAST'},
                searchTag:'tag',
                searchText:'stuff',
                selectedPlatform : 'Linux'
            };

            expect(controller.awsTable.data).toEqual([]);

            controller.searchAWSInstances();

            expect(gkAWSService.search).toHaveBeenCalledWith({
                account: controller.forms.awsInstanceForm.selectedAccount.alias.toLowerCase(),
                region: controller.forms.awsInstanceForm.selectedRegion.name,
                searchTag:controller.forms.awsInstanceForm.searchTag,
                searchStr:controller.forms.awsInstanceForm.searchText,
                platform:controller.forms.awsInstanceForm.selectedPlatform
            });

            let error = '';
            try {
                scope.$apply();
            }catch(e){
                error = e;
            }
            if(happy) {
                expect(controller.awsTable.data).toEqual(response.data);
            }else {
                expect(controller.awsTable.data).toEqual([]);
            }
        };

        let grantAccess = (pressOk, happy) => {
            spyOn($mdToast, 'simple').and.callThrough();

            //fake the dialog
            let deferred = $q.defer();
            spyOn($mdDialog, 'show').and.returnValue(deferred.promise);
            let resp = {explanation:'test', ticketId:'TEST-123'};
            pressOk ? deferred.resolve(resp) : deferred.reject(resp);

            //fake the rest call
            let gdefer = $q.defer();
            spyOn(gkGrantService, 'post').and.returnValue(gdefer.promise);
            happy ? gdefer.resolve() : gdefer.reject({message:"hello"});

			
            controller.usersTable = {
                selected:[
                    { name:'abc', email:'abc@abc.com'},
                    { name:'def', email:'def@abc.com'}
                ]};

            controller.awsTable = {
                selected:[
                    {name:'awslx', id:"abc", application:'ABC', ip:'127.0.0.1'}
                ]};

            controller.forms.grantForm = {
                $valid:true,
                grantValue:5
            };

            controller.forms.awsInstanceForm = {
                selectedAccount: {
                    sdlc:'TEST',
                    alias:'TheACCOUNT'
                },
                selectedRegion: {
                    name:'us-west-2'
                },
                selectedPlatform: 'Test Platform',
            };

            let justification = {
                ticketId: 'TEST-123',
                explanation: 'test'
            };
            controller.grantAccess();

            let error;
            try {
                scope.$apply();
            }catch(e){
                error = e;
            }
            expect($mdDialog.show).toHaveBeenCalled();
            if(pressOk){
                expect(gkGrantService.post).toHaveBeenCalledWith(controller.forms.grantForm.grantValue, controller.usersTable.selected,
                    controller.forms.awsInstanceForm.selectedAccount.alias.toLowerCase(), controller.forms.awsInstanceForm.selectedRegion.name, controller.awsTable.selected, justification.ticketId, justification.explanation, controller.forms.awsInstanceForm.selectedPlatform);
            }else{
                expect(gkGrantService.post).not.toHaveBeenCalled();
            }
            if(happy) {
                expect(error).toBeUndefined();
                expect($mdToast.simple).toHaveBeenCalled();
            }else {
                if(pressOk){
                    expect(controller.error).toBeDefined();
                    expect($mdToast.simple).toHaveBeenCalled();
                }else{
                    expect($mdToast.simple).not.toHaveBeenCalled();
                }
            }
        };

        it('Should Initialize properly', function(){
            testInit(true);
        });

        it('Test Account Fetch going bad', function(){
            testInit(false);
        });

        it('searchAWSInstances - happy ', function(){
            searchAWSInstances(true);
        });

        it('searchAWSInstances - bad ', function(){
            searchAWSInstances(false);
        });

        it('grantAccess - push OK + happy ', function(){
            testInit(true);
			setTimeout(function(){
				grantAccess(true, true);
			},1000);
            
        });

        it('grantAccess - push OK + fail', function(){
            testInit(true);
            grantAccess(true, false);
        });


    });

    describe('Dirty check should be performed and prompt displayed',function(){
        beforeEach(inject(function(_$mdDialog_, _$mdToast_, $http, _$q_, _$httpBackend_,_$rootScope_){
            $mdDialog = _$mdDialog_;
            $mdToast = _$mdToast_;
            gkAWSService = new AWSDataService($http);
            gkADService  = new ADDataService($http);
            gkGrantService = new GrantDataService($http);
            gkAccountService = new AccountDataService($http);
            $q = _$q_;
            $httpBackend = _$httpBackend_;
            $rootScope=_$rootScope_;
            $scope=$rootScope.$new();
            $state = {go:function(){}};
            $rootScope.userInfo = {
                userId:"testId",
                user:"test",
                role:"approver",
                email:"test@email.com",
                memberships:['testApplication'],
                approvalThreshold:20
            };
            $httpBackend.expectGET('/api/gatekeeper/getAccounts').respond([]);


            controller = new GatekeeperSelfServiceController($mdDialog, $mdToast, gkADService,$scope,$state,$rootScope, gkEc2ConfigService);
        }));

        let confirmStateChange = () => {
            let deferred = $q.defer();
            spyOn($mdDialog, 'show').and.returnValue(deferred.promise);
            $scope.$broadcast('$stateChangeStart');
            expect($mdDialog.show).toHaveBeenCalled();
        };

        let checkPrompt = (confirm) =>{
            controller.forms.adForm = {
                $valid: true,
                searchText:'steve',
                $dirty:true
            };
            let deferred = $q.defer();
            spyOn($mdDialog, 'show').and.returnValue(deferred.promise);
            spyOn($state, 'go');

            $scope.$broadcast('$stateChangeStart');
            spyOn($rootScope, '$broadcast');

            confirm ? deferred.resolve() : deferred.reject();
            $scope.$apply();
            if(confirm){
                expect($state.go).toHaveBeenCalled();
            }else{
                expect($state.go).not.toHaveBeenCalled();
                expect($rootScope.$broadcast).toHaveBeenCalledWith('stateChangeInterrupted');
            }


       }

        it('Prompt should be displayed when a form is dirty - AD',function(){
            controller.forms.adForm = {
                $valid: true,
                searchText:'steve',
                $dirty:true
            };
            confirmStateChange();
        });

        it('Prompt should be displayed when a form is dirty - AWS',function(){
            controller.forms.awsInstanceForm = {
                $valid: true,
                selectedAccount:{sdlc:'DEV'},
                selectedRegion:{name:'US-EAST'},
                searchTag:'tag',
                searchText:'stuff',
                platform: 'Linux',
                $dirty:true
            };
            confirmStateChange();
        });

        it('Prompt should be displayed when a form is dirty - Time',function(){
            controller.forms.grantForm = {
                $valid:true,
                grantValue:5,
                $dirty:true
            };
            confirmStateChange();
        });

        it('Clicking cancel on dialog results in state not changing',function(){
            checkPrompt(false);

        });

        it('Clicking ok on dialog results in state changing',function(){
            checkPrompt(true);
        });
    });
});
