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

import md from 'angular-material';
import gkUtil from '../shared/index';
import gkEc2Ctrl from './GatekeeperEc2Controller';
import gkEc2SsCtrl from './selfservice/Ec2SelfServiceController';
import gkEc2RequestCtrl from './request/Ec2RequestController';
import gkRequestHistoryCtrl from './request/Ec2RequestHistoryController';
import util from '../shared/generic/DirectiveUtils';
import Directive from '../shared/generic/BaseDirective';

var gateKeeperModule = angular.module('gatekeeper-ec2', [md, gkUtil]);

gateKeeperModule.controller('gkEc2Controller', gkEc2Ctrl)
    .controller('gkEc2SelfServiceController', gkEc2SsCtrl)
    .controller('gkEc2RequestController', gkEc2RequestCtrl)
    .controller('gkEc2RequestHistoryController', gkRequestHistoryCtrl)
    .directive('gatekeeperEc2Component',   util.newDirective(new Directive(require('./selfservice/template/gatekeeperAWSEc2Component.tpl.html'))))
    .directive('gatekeeperEc2GrantComponent', util.newDirective(new Directive(require('./selfservice/template/gatekeeperEc2GrantComponent.tpl.html'))));


export default gateKeeperModule.name;



