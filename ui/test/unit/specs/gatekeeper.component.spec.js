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

'use strict';

import module from '../../../app/component/gatekeeper';
import GatekeeperController from '../../../app/component/GatekeeperController';

describe('GateKeeper UI gatekeeper component', function () {
    let scope, controller, mockDataSvc, $state, deferred;

    beforeEach(angular.mock.module(module));

    beforeEach(inject(function ($rootScope) {
        scope = $rootScope.$new();
    }));

    describe('gkController ', function () {

        beforeEach(inject(function($q, $rootScope){
            $state = {go:function(){}};
            spyOn($state, 'go');
            mockDataSvc = {fetch: function(){}};
            deferred = $q.defer();
            spyOn(mockDataSvc, 'fetch').and.returnValue(deferred.promise);

            controller = new GatekeeperController($state, mockDataSvc, mockDataSvc, scope, $rootScope);
        }));

        it('ready() will return false if user is not set and should return true if user is set', inject(function(){
            var mockResp = {data:
                    {
                        name:'test guy',
                        role:"SUPPORT",
                        email:"test@place.com",
                        memberships:['Application'],
                        userId: 'tguy'
                    }
            };
            console.log(scope);
            deferred.resolve(mockResp);
            expect(controller.ready()).toBeFalsy();
            scope.$apply();
            expect(controller.ready()).toBeTruthy();
        }));

        it('ready() will return false if user set to unauthorized', inject(function(){
            var mockResp = {data:
                    {
                        name:'test guy',
                        role:"UNAUTHORIZED",
                        email:"test@place.com",
                        agsRoles: [],
                        memberships:['Application'],
                        userInfo: {
                            role: "SUPPORT"
                        },
                        userId: 'tguy'

                    }
            };
            deferred.resolve(mockResp);
            expect(controller.ready()).toBeFalsy();
            scope.$apply();
            expect(controller.ready()).toBeFalsy();
        }));

        it('should reset index on stateChangeInterrupted event', inject(function(){
            controller.global.rollbackIndex=3;
            controller.global.selectedIndex=2;
            scope.$broadcast('stateChangeInterrupted');
            expect(controller.global.selectedIndex).toBe(3);
        }));

        it('should update the fallback index on $stateChangeSuccess event', inject(function(){
            controller.global.rollbackIndex=3;
            controller.global.selectedIndex=2;
            scope.$broadcast('$stateChangeSuccess');
            expect(controller.global.rollbackIndex).toBe(2);
        }));
    });
});
