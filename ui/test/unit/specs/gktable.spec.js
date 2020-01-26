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

import module from '../../../app/common-web/table';

describe('GKTable Directive', () => {
    let element, scope, compile;

    function compileDirective() {
        compile(element)(scope);
        scope.$digest();
    }

    beforeEach(angular.mock.module(module));

    beforeEach(inject(($compile, $rootScope) => {
        element = angular.element('<gk-table config="config"></gk-table>');
        scope = $rootScope.$new();
        compile = $compile;
    }));

    describe('Export Button', () => {
        beforeEach(() => {
            scope.config = {
                selection: 'none',
                toolbar:{
                    header: 'Test',
                },
            };
        });

        it('should not show up when export attribute is not specified', () => {
            compileDirective();
            expect(element.find('button').length).toBe(0);
        });

        it('should be disabled while data is loading', () => {
            scope.config.fetching = true;
            scope.config.export = {};
            compileDirective();

            expect(element.find('button').length).toBe(1);
            expect(element.find('button').attr('disabled')).toBeTruthy();
        });

        it('should trigger export function on the controller upon click', () => {
            scope.config.export = {};
            scope.config.data = [];
            compileDirective();

            const innerScope = element.isolateScope();
            const innerController = innerScope.ctrl;
            spyOn(innerController, 'export');

            expect(element.find('button').length).toBe(1);
            expect(element.find('button').attr('disabled')).toBeFalsy();

            element.find('button').triggerHandler('click');
            expect(innerController.export).toHaveBeenCalled();
        });
    });

})