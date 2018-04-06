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

import envMocks from './mocks/env.mocks';
import roleMocks from './mocks/role.mocks';
import adMocks from './mocks/ad.mocks';
import awsMocks from './mocks/aws.mocks.js';
import grantMocks from './mocks/grant.mocks';
import requestMocks from './mocks/request.mocks';
import rdsRequestMocks from './mocks/rds.request.mocks';

function runBase($rootScope, $state){
    require('../node_modules/angular-material-data-table/dist/md-data-table.min.css');

    //Nullify out the back button and entering the url from the app.
    $rootScope.$on('$stateChangeStart', (event, toState, toParams, fromState, fromParams) => {
        if(toState.name === 'gk' && fromState.name.length !== 0){
            event.preventDefault();
        }
    });
}

function runMocked($rootScope, $state, $httpBackend){
    runBase($rootScope, $state);

    $httpBackend.whenGET(/.*\.tpl\.html/).passThrough();
    $httpBackend.whenGET(/.*\.svg/).passThrough();
    $httpBackend.whenGET(/.*\.css/).passThrough();
    
    // $httpBackend.whenGET('/api/gatekeeper/getAccounts').respond(envMocks.envResp);
    // $httpBackend.whenGET('/api/gatekeeper/auth/getRole').respond(roleMocks.approver);
    // $httpBackend.whenGET(/\/api\/gatekeeper\/searchAD\?searchStr=.{2,}/).respond(adMocks.adResp);
    // $httpBackend.whenGET(/\/api\/gatekeeper\/searchAD\?searchStr=1/).respond(adMocks.adResp2);
    // $httpBackend.whenGET(/\/api\/gatekeeper\/searchAD\?searchStr=2/).respond(adMocks.adResp3);
    // $httpBackend.whenGET(/\/api\/gatekeeper\/searchAWSInstances.*/).respond(awsMocks.awsResp);
    // $httpBackend.whenPOST(/\/api\/gatekeeper\/grantAccess/).respond(grantMocks.grantResp);
    // $httpBackend.whenGET('/api/gatekeeper/getActiveRequests').respond(requestMocks.requestResp);
    // $httpBackend.whenGET('/api/gatekeeper/getCompletedRequests').respond(requestMocks.requestResp);
    // $httpBackend.whenPUT('/api/gatekeeper/approveRequest').respond(requestMocks.success);
    // $httpBackend.whenPUT('/api/gatekeeper/rejectRequest').respond(requestMocks.success);
    $httpBackend.whenPOST(/\/api\/gatekeeper-ec2\/grantAccess/).passThrough();
    $httpBackend.whenGET('/api/gatekeeper-ec2/getAccounts').passThrough();
    $httpBackend.whenGET('/api/gatekeeper-ec2/auth/getRole').passThrough();
    $httpBackend.whenGET(/\/api\/gatekeeper-ec2\/searchAD.*/).passThrough();
    $httpBackend.whenGET(/\/api\/gatekeeper-ec2\/searchAD.*/).passThrough();
    $httpBackend.whenGET(/\/api\/gatekeeper-ec2\/searchAWSInstances.*/).passThrough();
    $httpBackend.whenGET(/api\/register\/apps/).passThrough();
    $httpBackend.whenGET('/api/gatekeeper-ec2/getActiveRequests').passThrough();
    $httpBackend.whenGET('/api/gatekeeper-ec2/getCompletedRequests').passThrough();
    $httpBackend.whenPUT('/api/gatekeeper-ec2/approveRequest').passThrough();
    $httpBackend.whenPUT('/api/gatekeeper-ec2/rejectRequest').passThrough();

    $httpBackend.whenGET('/api/gatekeeper-rds/getAccounts').passThrough();
    $httpBackend.whenGET('/api/gatekeeper-rds/auth/getRole').passThrough();
    $httpBackend.whenGET('/api/gatekeeper-rds/getActiveRequests').respond(rdsRequestMocks.requestResp);
    $httpBackend.whenGET('/api/gatekeeper-rds/getCompletedRequests').respond(rdsRequestMocks.requestResp);


}

export default runMocked;
