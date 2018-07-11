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

import Directive from '../shared/generic/BaseDirective';

/**
 * Navigation directive
 *
 * @returns {{}}
 *
 * TODO: may need to do some fine tuning with this one.
 */

class GatekeeperNavBar extends Directive {
    constructor(template){
        super(template);
        this.scope = {
            selectedIndex: "=",
            tabData: "=",
            states: "=",
            selectedView: "=",
            readyFunc: "&",
            userInfo: '='
        };

        this.controllerAs = 'gkNavCtrl';
    }

    controller($scope, $state){
        this.openView = (transition) => {
            if($scope.readyFunc()) {
                $state.go(transition, {userId: $scope.userInfo.userId, user: $scope.userInfo.user, role: $scope.userInfo.role, email: $scope.userInfo.email, approvalThreshold:$scope.userInfo.approvalThreshold, memberships:$scope.userInfo.memberships});
            }else{
                $state.go($scope.states.error);
            }
        };

        this.switchContext = (context) => {
            if($scope.readyFunc() && !$state.current.name.includes(context)){
                if(!$scope.tabData.enabled){
                    //if the tab is not enabled just go to the tab at position 0
                    $scope.selectedIndex = 0;
                }
                $state.go(context+"."+Object.keys($scope.tabData)[$scope.selectedIndex].toLowerCase(), { selectedIndex: $scope.selectedIndex });
            }
        };
    }
}

export default GatekeeperNavBar;