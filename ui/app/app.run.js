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

function runBase($rootScope, $state){
    require('../node_modules/angular-material-data-table/dist/md-data-table.min.css');
    require('../node_modules/hover.css/css/hover-min.css');
    //Nullify out the back button and entering the url from the app.
    $rootScope.$on('$stateChangeStart', (event, toState, toParams, fromState, fromParams) => {
        if(toState.name === 'gk' && fromState.name.length !== 0){
            event.preventDefault();
        }
    });
}

export default runBase;
