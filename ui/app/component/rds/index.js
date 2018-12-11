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
import gkRdsCtrl from './GatekeeperRdsController';
import util from '../shared/generic/DirectiveUtils';
import Directive from '../shared/generic/BaseDirective';
import GkRdsSearch from './selfservice/GkRdsSearch';
import GkRdsCheckbox from './selfservice/GkRdsCheckbox';
import gkRdsSelfServiceCtrl from './selfservice/RdsSelfServiceController';
import gkRdsSchemaDialogController from './selfservice/RdsSchemaDialogController';
import gkRdsRequestCtrl from './request/RdsRequestController';
import gkRdsHistoryRequestCtrl from './request/RdsRequestHistoryController';
import gkRdsAdminCtrl from './admin/RdsAdminController';

var gateKeeperModule = angular.module('gatekeeper-rds', [md, gkUtil]);

gateKeeperModule.controller('gkRdsController', gkRdsCtrl)
    .controller('gkRdsSelfServiceController', gkRdsSelfServiceCtrl)
    .controller('gkRdsRequestController', gkRdsRequestCtrl)
    .controller('gkRdsRequestHistoryController', gkRdsHistoryRequestCtrl)
    .controller('gkRdsSchemaDialogController', gkRdsSchemaDialogController)
    .controller('gkRdsAdminController', gkRdsAdminCtrl)
    .directive('gatekeeperRoleCheckbox', util.newDirective(new GkRdsCheckbox(require('./selfservice/template/gatekeeperRoleCheckbox.tpl.html'))))
    .directive('gatekeeperRdsComponent',   util.newDirective(new GkRdsSearch(require('./selfservice/template/gatekeeperAWSRdsComponent.tpl.html'))))
    .directive('gatekeeperRdsGrantComponent', util.newDirective(new Directive(require('./selfservice/template/gatekeeperRdsGrantComponent.tpl.html'))));


export default gateKeeperModule.name;



