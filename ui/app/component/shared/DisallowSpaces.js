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
import $ from 'jquery';
/**
 * Navigation directive
 */

class DisallowSpaces extends Directive {
    constructor(template){
        super(template);
        this.restrict = 'A';

        this.controllerAs = 'gkNavCtrl';
    }

    link($scope, $element) {
        $element.bind('input', function() {

            $(this).val($(this).val().replace(/ /g, ''));
        });
    }
}

export default DisallowSpaces;