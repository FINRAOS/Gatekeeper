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

class GkRdsCheckbox extends Directive{
    constructor(template){
        super(template);
        this.scope = {
            formObj: '=',
            formAttr: '@',
            label: '@',
            disableFn: '=',
        };
        this.transclude = true;
        this.controllerAs="ctrl";
    }

    controller($scope){
        let vm = this;

        if(!$scope.formObj){
            $scope.formObj = {};
        }

        vm.updateForm = (value) => {
            $scope.formObj[$scope.formAttr] = value;
        };

        /**
         * Check whether to disable a checkbox. If a checkbox should be disabled then it will clear the value inside that checkbox as well.
         * @returns {*}
         */
        vm.disable = () => {
            if(!$scope.formObj){
                return true;
            }

            let disabled = $scope.disableFn();
            if(disabled) {
                $scope.value = false;
                $scope.formObj[$scope.formAttr] = false;
            }
            return disabled;
        };
    }

}

export default GkRdsCheckbox;



