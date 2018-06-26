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
import table from 'angular-material-data-table';
import Directive from './generic/BaseDirective'
import ADDataService from './ADDataService';
import AWSDataService from './AWSDataService';
import RDSDataService from './RDSDataService';
import RdsRevokeUsersDataService from './RdsRevokeUsersDataService';
import RdsUsersDataService from './RdsUsersDataService';
import AccountDataService from './AccountDataService';
import GrantDataService from './GrantDataService';
import RdsGrantDataService from './RdsGrantDataService';
import RoleDataService from './RoleDataService';
import RequestDataService from './RequestDataService';
import SchemaDataService from './SchemaDataService';
import GkNavBar from './GkNavBar';
import util from './generic/DirectiveUtils';

import gkSSCtrl from './selfservice/GatekeeperSelfServiceController';

var gkUtil = angular.module('gatekeeper-util', [md, table])
    .service('gkRoleService', RoleDataService)
    .service('gkADService', ADDataService)
    .service('gkAWSService', AWSDataService)
    .service('gkRDSService', RDSDataService)
    .service('gkRdsUsersService', RdsUsersDataService)
    .service('gkRdsRevokeUsersService', RdsRevokeUsersDataService)
    .service('gkGrantService', GrantDataService)
    .service('gkRdsGrantService', RdsGrantDataService)
    .service('gkAccountService', AccountDataService)
    .service('gkRequestService', RequestDataService)
    .service('gkSchemaService', SchemaDataService)
    .controller('gkSelfServiceController', gkSSCtrl)
    .directive('gatekeeperUserComponent',  util.newDirective(new Directive(require('./selfservice/template/gatekeeperADComponent.tpl.html'))))
    .directive('gkNavBar', util.newDirective(new GkNavBar(require('./template/gkNavBar.tpl.html'))));


export default gkUtil.name;



