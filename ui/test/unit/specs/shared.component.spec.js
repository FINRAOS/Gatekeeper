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
import ADDataService from '../../../app/component/shared/ADDataService';
import AWSDataService from '../../../app/component/shared/AWSDataService';
import GrantDataService from '../../../app/component/shared/GrantDataService';
import RoleDataService from '../../../app/component/shared/RoleDataService';
import RequestDataService from '../../../app/component/shared/RequestDataService';

//Controllers
import md from 'angular-material';


describe('GateKeeper UI shared gatekeeper component', function () {

    let $http, scope, controller, $state;

    let testDataService = (Obj, method, value) => {
        spyOn($http, 'get').and.returnValue('stubbed');
        let dataservice = new Obj($http, $state);
        let promise = dataservice[method](value);
        expect(promise).toBe('stubbed');
        if(!value) {
            expect($http.get).toHaveBeenCalledWith('/api/gatekeeper-ec2/' + dataservice.resource);
        }else{
            var searchString = '?';
            var idx = 0;
            angular.forEach(value, function(v, k, index){
                searchString += k + '=' + v;
                if(idx++ !== Object.keys(value).length - 1){
                    searchString += '&';
                }
            });
            expect($http.get).toHaveBeenCalledWith('/api/gatekeeper-ec2/' + dataservice.resource + searchString);
        }
    };

    let testDataServiceErr = (Obj, value) => {
        let exErrStr = 'SEARCH: searchParams must be object containing the following values ';
        let dataservice = new Obj($http, $state);
        expect(function(){dataservice.search(value);}).toThrow(exErrStr + dataservice.params);

    };

    beforeEach(angular.mock.module(md));

    beforeEach(inject(function($rootScope){
        scope = $rootScope.$new();
        $state = {go:function(){}};
    }));

    describe('DataServices', function () {
        beforeEach(inject(function(_$http_){
            $http = _$http_;
            $state = {go:function(){}, current:{name:'gatekeeper.ec2'}};
        }));

        it('AccountDataService should call fetch properly and return a promise', function () {
            testDataService(AccountDataService, 'fetch');
        });

        it('ADDataService should call fetch properly and return a promise', function () {
            testDataService(ADDataService, 'search', {searchStr:'test'});
        });

        it('AWSDataService should call search properly and return a promise', function () {
            testDataService(AWSDataService, 'search', {'account':'dev','region':'east','searchTag':'tag','searchStr':'test', 'platform':'testPlatform'});
        });

        it('ADDataService should throw an error if search is incorrectly called', function () {
            testDataServiceErr(ADDataService, {});
            testDataServiceErr(ADDataService, 'hey!');
        });

        it('AWSDataService throw an error if search is incorrectly called', function () {
            testDataServiceErr(AWSDataService, {});
            testDataServiceErr(AWSDataService, 'hey!');
        });

        it('RoleDataService should call fetch properly and return a promise', function () {
            testDataService(RoleDataService, 'fetch');
        });

        it('GrantDataService should call fetch properly and return a promise', function () {
            spyOn($http, 'post').and.returnValue('stubbed');

            let dataservice = new GrantDataService($http, $state);
            var testBundle = {hours:5, users:{name:'bob', email:'bob@thing' }, account:"TEST", region:"us-west-2", instances:{instance:'hello'}, ticketId: 'TST-123', requestReason:'test request explanation', platform: "Test platform"};
            let promise = dataservice.post(testBundle.hours, testBundle.users, testBundle.account, testBundle.region, testBundle.instances, testBundle.ticketId, testBundle.requestReason, testBundle.platform);
            expect(promise).toBe('stubbed');
            expect($http.post).toHaveBeenCalledWith('/api/gatekeeper-ec2/grantAccess', testBundle);
        });

        it('RequestDataService should call getActive properly and return a promise', function(){
            spyOn($http, 'get').and.returnValue('stubbed');
            let dataservice = new RequestDataService($http, $state);
            let promise = dataservice.getActive();
            expect(promise).toBe('stubbed');
            expect($http.get).toHaveBeenCalledWith('/api/gatekeeper-ec2/getActiveRequests');
        });

        it('RequestDataService should call getCompleted properly and return a promise', function(){
            spyOn($http, 'get').and.returnValue('stubbed');
            let dataservice = new RequestDataService($http, $state);
            let promise = dataservice.getCompleted();
            expect(promise).toBe('stubbed');
            expect($http.get).toHaveBeenCalledWith('/api/gatekeeper-ec2/getCompletedRequests');
        });

        it('RequestDataService should call approve properly and return a promise', function(){
            spyOn($http, 'put').and.returnValue('stubbed');
            let dataservice = new RequestDataService($http, $state);
            let promise = dataservice.approve("TestObj");
            expect(promise).toBe('stubbed');
            expect($http.put).toHaveBeenCalledWith('/api/gatekeeper-ec2/approveRequest', 'TestObj');
        });

        it('RequestDataService should call reject properly and return a promise', function(){
            spyOn($http, 'put').and.returnValue('stubbed');
            let dataservice = new RequestDataService($http, $state);
            let promise = dataservice.reject("TestObj");
            expect(promise).toBe('stubbed');
            expect($http.put).toHaveBeenCalledWith('/api/gatekeeper-ec2/rejectRequest', 'TestObj');
        });

    });
});
