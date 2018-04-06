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

import util from '../shared/generic/DirectiveUtils';
import gkSelectCtrl from './GatekeeperSelectController'
import GatekeeperSelectButton from './GatekeeperSelectButton';

var gatekeeperSelectModule = angular.module('gatekeeper-select', [])
    .controller('gkSelectController', gkSelectCtrl)
    .directive('gatekeeperSelectButton',  util.newDirective(new GatekeeperSelectButton(require('./template/selectbutton.tpl.html'))));

export default gatekeeperSelectModule.name;
