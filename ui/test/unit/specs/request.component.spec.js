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

//Mocks
import requestMocks from '../../mocks/request.mocks'

//Dependencies
import md from 'angular-material';
import router from 'angular-ui-router';

//Services
import RequestDataService from '../../../app/component/shared/RequestDataService';

//Controllers
import GatekeeperRequestController from '../../../app/component/ec2/request/Ec2RequestController';
import GatekeeperRequestDialogController from '../../../app/component/shared/request/GatekeeperRequestDialogController';
import GatekeeperRequestDialogRequestorController from '../../../app/component/shared/request/GatekeeperRequestDialogRequestorController';
import GatekeeperRequestDialogAdminController from '../../../app/component/shared/request/GatekeeperRequestDialogAdminController';
import GatekeeperRequestHistoryController from '../../../app/component/ec2/request/Ec2RequestHistoryController';
import Ec2RequestDialogAdminController from "../../../app/component/ec2/request/Ec2RequestDialogAdminController";

describe('GateKeeper UI Request Component', function () {

    let $rootScope, $httpBackend, $mdDialog, $mdToast, $state, scope, deferred, controller;

    beforeEach(angular.mock.module(md, router));

    beforeEach(inject(function(_$rootScope_, _$httpBackend_, _$q_, _$state_){
        $rootScope = _$rootScope_;
        scope = $rootScope.$new();
        $httpBackend = _$httpBackend_;
        $q = _$q_;
        $state = _$state_;
        $state.current.name = 'gatekeeper.ec2';

    }));

    //mock all this stuff out.
    let $q,gkRequestService, gkRequestController;

    let gkTestInitNonApprove = (happy) => {
        //completely mock out the api call
        let resp = {data: requestMocks.requestResp};

        if(happy) {
            deferred.resolve(resp);
        }else{
            deferred.reject(resp);
        }

        let expectedTable = {
            selection:'dialog',
            template: require('../../../app/component/ec2/request/template/request.tpl.html'),
            templateController: GatekeeperRequestDialogRequestorController,
            templateControllerAs: 'dialogCtrl',
            headers:[
                {dataType: 'number', display:'Request ID', value:'id'},
                {dataType: 'string', display:'Account', value: 'account'},
                {dataType: 'string', display:'Region', value: 'region'},
                {dataType: 'string', display:'Requestor Name', value:'requestorName'},
                {dataType: 'string', display:'Requestor Email', value:'requestorEmail'},
                {dataType: 'number', display:'Hours', value:'hours'},
                {dataType: 'number', display:'Users', value:'userCount'},
                {dataType: 'number', display:'Instances', value:'instanceCount'}
            ],
            toolbar:{
                header: "Current Active Requests",
                inlineFilter:true
            },
            data: [],
            selected: [],
            query:{
                order: 'name',
                limit: 20,
                page: 1
            },
            pagination: {
                pageSelect: true,
                limitOptions: [5, 10, 20, 40]
            }
        };

        expect(gkRequestController.headerLabel).toEqual("Active Requests");
        expect(expectedTable.selection).toEqual(gkRequestController.requestTable.selection);
        expect(expectedTable.template).toEqual(gkRequestController.requestTable.template);
        expect(expectedTable.templateController).toEqual(gkRequestController.requestTable.templateController);
        expect(expectedTable.templateControllerAs).toEqual(gkRequestController.requestTable.templateControllerAs);
        expect(expectedTable.headers).toEqual(gkRequestController.requestTable.headers);
        expect(expectedTable.toolbar).toEqual(gkRequestController.requestTable.toolbar);
        expect(expectedTable.data).toEqual(gkRequestController.requestTable.data);
        expect(expectedTable.selected).toEqual(gkRequestController.requestTable.selected);
        expect(expectedTable.query).toEqual(gkRequestController.requestTable.query);
        expect(expectedTable.pagination).toEqual(gkRequestController.requestTable.pagination);
        scope.$apply();
        if(happy) {
            expect(gkRequestController.fetched).toBeTruthy();
            expect(gkRequestController.requestTable.data).toEqual(requestMocks.requestResp);
        }else{
            expect(gkRequestController.fetched).toBeFalsy();
            expect(gkRequestController.error).toBeDefined();
        }
    };

    let gkTestInitApprove = (happy) => {
        let resp = {data: requestMocks.requestResp};

        if(happy) {
            deferred.resolve(resp);
        }else{
            deferred.reject(resp);
        }

        let expectedTable = {
            selection:'dialog',
            template:require('../../../app/component/ec2/request/template/request.tpl.html'),
            templateController: Ec2RequestDialogAdminController,
            templateControllerAs: 'dialogCtrl',
            headers:[
                {dataType: 'number', display:'Request ID', value:'id'},
                {dataType: 'string', display:'Account', value: 'account'},
                {dataType: 'string', display:'Region', value: 'region'},
                {dataType: 'string', display:'Requestor Name', value:'requestorName'},
                {dataType: 'string', display:'Requestor Email', value:'requestorEmail'},
                {dataType: 'number', display:'Hours', value:'hours'},
                {dataType: 'number', display:'Users', value:'userCount'},
                {dataType: 'number', display:'Instances', value:'instanceCount'}
            ],
            toolbar:{
                header: "Current Active Requests",
                inlineFilter:true
            },
            data: [],
            selected: [],
            query:{
                order: 'name',
                limit: 20,
                page: 1
            },
            pagination: {
                pageSelect: true,
                limitOptions: [5, 10, 20, 40]
            }
        };

        expect(gkRequestController.headerLabel).toEqual("Active Requests");
        expect(expectedTable.selection).toEqual(gkRequestController.requestTable.selection);
        expect(expectedTable.template).toEqual(gkRequestController.requestTable.template);
        expect(expectedTable.templateController).toEqual(gkRequestController.requestTable.templateController);
        expect(expectedTable.templateControllerAs).toEqual(gkRequestController.requestTable.templateControllerAs);
        expect(expectedTable.headers).toEqual(gkRequestController.requestTable.headers);
        expect(expectedTable.toolbar).toEqual(gkRequestController.requestTable.toolbar);
        expect(expectedTable.data).toEqual(gkRequestController.requestTable.data);
        expect(expectedTable.selected).toEqual(gkRequestController.requestTable.selected);
        expect(expectedTable.query).toEqual(gkRequestController.requestTable.query);
        expect(expectedTable.pagination).toEqual(gkRequestController.requestTable.pagination);
        scope.$apply();
        if(happy) {
            expect(gkRequestController.fetched).toBeTruthy();
            expect(gkRequestController.requestTable.data).toEqual(requestMocks.requestResp);
        }else{
            expect(gkRequestController.fetched).toBeFalsy();
            expect(gkRequestController.error).toBeDefined();
        }
    };

    /**
     * GatekeeperRequestController Tests
     */

    describe('GatekeeperRequestController - Non-Approver', function(){
        beforeEach(inject(function($http){
            gkRequestService = new RequestDataService($http);
            deferred = $q.defer();
            $rootScope.userInfo = {
                role: "SUPPORT"
            };
            spyOn(gkRequestService, 'getActive').and.returnValue(deferred.promise);
            $httpBackend.whenGET('/api/gatekeeper-ec2/getActiveRequests').respond(requestMocks.requestResp);
            gkRequestController = new GatekeeperRequestController(gkRequestService, $rootScope);
        }));

        it('Should Initialize properly', function(){
            gkTestInitNonApprove(true);
        });
        it('Should Initialize properly - fails', function(){
            gkTestInitNonApprove(false);
        });
    });

    describe('GatekeeperRequestController - Approver', function(){
        beforeEach(inject(function($http){
            gkRequestService = new RequestDataService($http);
            $rootScope.userInfo = {
                role: "APPROVER"
            };

            deferred = $q.defer();
            spyOn(gkRequestService, 'getActive').and.returnValue(deferred.promise);
            $httpBackend.whenGET('/api/gatekeeper-ec2/getActiveRequests').respond(requestMocks.requestResp);
            gkRequestController = new GatekeeperRequestController(gkRequestService, $rootScope);
        }));

        it('Should Initialize properly', function(){
            gkTestInitApprove(true);
        });
        it('Should Initialize properly - fails', function(){
            gkTestInitApprove(false);
        });
    });

    /**
     * GatekeeperRequestHistoryController Tests
     */

    let gkTestInitHistory = (happy) => {
        let resp = {data: requestMocks.requestResp};

        if(happy) {
            deferred.resolve(resp);
        }else{
            deferred.reject(resp);
        }


        let expectedTable = {
            selection:'dialog',
            template:require('../../../app/component/ec2/request/template/request.tpl.html'),
            templateController: GatekeeperRequestDialogController,
            templateControllerAs: 'dialogCtrl',
            toolbar:{
                header: "Recently Handled Requests",
                inlineFilter:true,
                selectFilters: [
                    {
                        label: 'Environment',
                        options: [],
                        filterFn: gkRequestController.filterEnvironment,
                        width: '118px'
                    }
                ]
            },
            headers:[
                {dataType: 'date', config: {dateFormat:'short'}, display:'Created', value:"created"},
                {dataType: 'date', config: {dateFormat:'short'},display: 'Updated', value: 'updated' },
				{dataType: 'string', display:'Request ID', value:'id'},
                {dataType: 'string', display:'Environment/Account', value: 'account'},
                {dataType: 'string', display:'Requestor Name', value:'requestorName'},
                {dataType: 'string', display:'Requestor Email', value:'requestorEmail'},
                {dataType: 'number', display:'Hours Requested', value:'hours'},
                {dataType: 'number', display:'Users', value:'userCount'},
                {dataType: 'number', display:'Instances', value:'instanceCount'},
                {dataType: 'string', display:'Outcome', value:'status'}
            ],
            data: [],
            selected: [],
            query:{
                order: 'name',
                limit: 20,
                page: 1
            },
            pagination: {
                pageSelect: true,
                limitOptions: [5, 10, 20, 40]
            }
        };

        expect(gkRequestController.headerLabel).toEqual("Request History");
        expect(expectedTable.selection).toEqual(gkRequestController.requestTable.selection);
        expect(expectedTable.template).toEqual(gkRequestController.requestTable.template);
        expect(expectedTable.templateController).toEqual(gkRequestController.requestTable.templateController);
        expect(expectedTable.templateControllerAs).toEqual(gkRequestController.requestTable.templateControllerAs);
        expect(expectedTable.headers).toEqual(gkRequestController.requestTable.headers);
        expect(expectedTable.toolbar).toEqual(gkRequestController.requestTable.toolbar);
        expect(expectedTable.data).toEqual(gkRequestController.requestTable.data);
        expect(expectedTable.selected).toEqual(gkRequestController.requestTable.selected);
        expect(expectedTable.query).toEqual(gkRequestController.requestTable.query);
        expect(expectedTable.pagination).toEqual(gkRequestController.requestTable.pagination);
        scope.$apply();
        if(happy) {
            expect(gkRequestController.fetched).toBeTruthy();
            expect(gkRequestController.requestTable.data).toEqual(requestMocks.requestResp);
        }else{
            expect(gkRequestController.fetched).toBeFalsy();
            expect(gkRequestController.error).toBeDefined();

        }
    };

    describe('GatekeeperRequestHistoryController', function(){
        beforeEach(inject(function($http){
            gkRequestService = new RequestDataService($http);
            deferred = $q.defer();
            spyOn(gkRequestService, 'getCompleted').and.returnValue(deferred.promise);
            $httpBackend.whenGET('/api/gatekeeper/getCompletedRequests').respond(requestMocks.requestResp);
            gkRequestController = new GatekeeperRequestHistoryController(gkRequestService);
        }));

        it('Should Initialize properly', function(){
            gkTestInitHistory(true);
        });
        it('Should Initialize properly - fails', function(){
            gkTestInitHistory(false);
        });
    });

    /**
     * GatekeeperRequestDialogController tests
     */

    describe('GatekeeperRequestDialogController', function(){
        beforeEach(inject(function($http, _$mdDialog_, _$mdToast_){
            $mdDialog = _$mdDialog_;
            $mdToast = _$mdToast_;
            gkRequestService = new RequestDataService($http);
            $rootScope.userInfo = {
                role: "SUPPORT"
            };

            // deferred = $q.defer();
            // spyOn(gkRequestService, 'getActive').and.returnValue(deferred.promise);
            // $httpBackend.whenGET('/api/gatekeeper/getActiveRequests').respond(requestMocks.requestResp);
            gkRequestController = new GatekeeperRequestDialogController($rootScope, $mdDialog, $mdToast, gkRequestService, requestMocks.requestResp[0]);
        }));

        it('Should Initialize properly', function(){
            var expectedOptions = [
                {
                    label:'Close',
                    action:gkRequestController.closeDialog,
                    style:"md-primary"
                }
            ];

            expect(expectedOptions).toEqual(gkRequestController.actions);
            expect(requestMocks.requestResp[0]).toEqual(gkRequestController.row)
        });

        it('Should call $mdDialog dismiss when close is called', function(){
            spyOn($mdDialog, "hide");
            gkRequestController.closeDialog();
            expect($mdDialog.hide).toHaveBeenCalled();
        });
    });

    /**
     * GatekeeperRequestDialogAdminController tests
     */

    describe('GatekeeperRequestDialogAdminController', function(){
        beforeEach(inject(function($http, _$mdDialog_, _$mdToast_, _$state_){
            $mdDialog = _$mdDialog_;
            $mdToast = _$mdToast_;
            $state = _$state_;
            
            gkRequestService = new RequestDataService($http);
            $rootScope.userInfo = {
                role: "APPROVER"
            };

            deferred = $q.defer();
            spyOn(gkRequestService, 'approve').and.returnValue(deferred.promise);
            spyOn(gkRequestService, 'reject').and.returnValue(deferred.promise);
            $httpBackend.whenGET('/api/gatekeeper-ec2/approveRequest').respond(requestMocks.requestResp);
            gkRequestController = new Ec2RequestDialogAdminController($rootScope, $mdDialog, $mdToast, gkRequestService, requestMocks.requestResp[0]);
        }));

        it('Should Initialize properly', function(){
            var expectedOptions = [
                {
                    label:'Approve',
                    action:gkRequestController.approveRequest,
                    style: 'md-raised md-accent'
                }, {
                    label:'Reject',
                    action:gkRequestController.rejectRequest,
                    style: 'md-raised md-primary'
                }, {
                    label:'Close',
                    action:gkRequestController.closeDialog,
                    style:"md-primary"
                }
            ];

            expect(expectedOptions).toEqual(gkRequestController.actions);
        });

        it('Should call $mdDialog hide when close is called', function(){
            spyOn($mdDialog, "hide");
            gkRequestController.closeDialog();

        });
        
        it('Should call $mdDialog hide when approved', function(){
            spyOn($mdDialog, "hide");
            spyOn($state, "reload").and.returnValue("");
            spyOn($mdToast, "show").and.callThrough();;
            gkRequestController.approveRequest();
            deferred.resolve({});
            scope.$apply();
            expect($mdDialog.hide).toHaveBeenCalled();
            expect($mdToast.show).toHaveBeenCalled();

        });
        
        it('Should call $mdDialog hide when rejected', function(){
            spyOn($mdDialog, "hide");
            spyOn($state, "reload").and.returnValue("");
            spyOn($mdToast, "show").and.callThrough();
            gkRequestController.rejectRequest();
            deferred.resolve({});
            scope.$apply();
            expect($mdDialog.hide).toHaveBeenCalled();
            expect($mdToast.show).toHaveBeenCalled();
        });

        it('Should call $mdDialog hide when approved - negative', function(){
            spyOn($mdDialog, "hide");
            spyOn($mdDialog, "show").and.callThrough();
            gkRequestController.approveRequest();
            deferred.reject({});
            scope.$apply();
            expect($mdDialog.hide).toHaveBeenCalled();
            expect($mdDialog.show).toHaveBeenCalled();
        });

        it('Should call $mdDialog hide when rejected - negative', function(){
            spyOn($mdDialog, "hide");
            spyOn($mdDialog, "show").and.callThrough();
            gkRequestController.rejectRequest();
            deferred.reject({});
            scope.$apply();
            expect($mdDialog.hide).toHaveBeenCalled();
            expect($mdDialog.show).toHaveBeenCalled();
        });
    });
});

