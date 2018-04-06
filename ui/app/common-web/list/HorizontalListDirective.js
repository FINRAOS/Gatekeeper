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

import BaseDirective from '../utils/BaseDirective';

/**
 * A directive for a Horizontal List.
 */
class HorizontalListDirective extends BaseDirective{
    constructor(template){
        super(template);
        this.scope = {
            items: '=',
            xs: '@',
            sm: '@',
            md: '@',
            lg: '@',
            xl: '@',
            icon: '@',
            firstLine: '@',
            secondLine: '@',
            thirdLine: '@',
            color: '@',
            filterBy: '='
        }
    }

    link(scope){

        //Log error messages if required variables are not provided
        let checkReqVal = (value) => {
            if(!scope[value]){
                console.error("gk-horizontal-list: Required value: '" + value + "' is missing");
            }
        };

        let checkVal = (value, defaultVal) => {
            scope[value] = scope[value] ? scope[value] : defaultVal;
        };

        //items + firstLine should be provided
        checkReqVal('items');
        checkReqVal('firstLine');

        //set defaults for flexers if they are not provided
        checkVal('xs', '100');
        checkVal('sm', '50');
        checkVal('md', '33');
        checkVal('lg', '25');
        checkVal('xl', '20');

    }
}


export default HorizontalListDirective;



