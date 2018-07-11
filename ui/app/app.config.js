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

function configureMaterial($mdDateLocaleProvider, $mdThemingProvider, $mdIconProvider){
    $mdThemingProvider.definePalette('black', {
        '50': '000000',
        '100': 'ff4081', //promise bar - pink
        '200': '000000',
        '300': '000000', //chip
        '400': '000000',
        '500': '000000',//header + promise bar - black (has to be)
        '600': 'ff4081', //selection animation color
        '700': '000000',
        '800': '000000', //text
        '900': '000000',
        'A100': '000000',
        'A200': '000000',
        'A400': '000000',
        'A700': '000000',
        'contrastDefaultColor': 'light'
    });

    $mdThemingProvider.definePalette('white', {
        '50': 'ffffff', //background color
        '100': 'ffffff',
        '200': 'ffffff', //select highlight
        '300': 'rgb(103,103,103)', //chip
        '400': 'ffffff',
        '500': 'ffffff',
        '600': 'ffffff',
        '700': 'ffffff',
        '800': 'ffffff', //text
        '900': '000000', //select text
        'A100': 'ffffff', //card background
        'A200': 'ffffff',
        'A400': 'ffffff',
        'A700': 'ffffff',
        'contrastDefaultColor': 'dark'
    });

    $mdThemingProvider.theme('default')
        .primaryPalette('black')
        .accentPalette('pink')
        .backgroundPalette('white');
    
    $mdDateLocaleProvider.parseDate = (dateString) => moment(dateString).toDate();

}
function configureRouting($stateProvider, $urlRouterProvider){

    $urlRouterProvider.otherwise('/gk/select');

    function confirmAccess($state, $stateParams){
        if($stateParams.role === 'UNAUTHORIZED'){
            $state.go('gk.denied');
        }
    }

    $stateProvider.state('gk',
        {
            url: '/gk',
            template: require('./component/gatekeeper.tpl.html'),
            controller: 'gkController',
            controllerAs: 'gkCtrl',
            reload:true

        }
    );

    $stateProvider.state('gk.select',
        {
            url: '/select',
            template: require('./component/select/template/select.tpl.html'),
            controller: 'gkSelectController',
            controllerAs: 'gkCtrl',
            reload:true

        }
    );

    //EC2 STATES
    $stateProvider.state('gk.ec2',
        {
            url: '/ec2',
            template: require('./component/ec2/template/ec2.tpl.html'),
            controller: 'gkEc2Controller',
            controllerAs: 'gkEc2Ctrl',
            params: {
                'selectedIndex':-1,
                'selectedView':'ec2'
            },
            reload:true

        }
    );

    $stateProvider.state('gk.ec2.requests',
        {
            url: '/requests',
            template: require('./component/shared/request/template/gatekeeperRequest.tpl.html'),
            controller: 'gkEc2RequestController',
            controllerAs: 'gkAppCtrl',
            params: {
                'userId':'unknown',
                'user':'unknown',
                'role':'unauthorized',
                'email':'unknown'
            },
            onEnter:confirmAccess
        }
    );

    $stateProvider.state('gk.ec2.history',
        {
            url: '/history',
            template: require('./component/shared/request/template/gatekeeperRequest.tpl.html'),
            controller: 'gkEc2RequestHistoryController',
            controllerAs: 'gkAppCtrl',
            params: {
                'userId':'unknown',
                'user':'unknown',
                'role':'unauthorized',
                'email':'unknown'
            },
            onEnter:confirmAccess
        }
    );


    $stateProvider.state('gk.ec2.selfservice',
        {
            url: '/selfservice',
            template: require('./component/ec2/selfservice/template/ec2SelfService.tpl.html'),
            controller: 'gkEc2SelfServiceController',
            controllerAs: 'gkSelfServiceCtrl',
            params: {
                'userId':'unknown',
                'user':'unknown',
                'memberships':[],
                'approvalThreshold':[],
                'role':'unauthorized',
                'email':'unknown'
            },
            onEnter:confirmAccess
        }
    );

    //RDS STATES
    $stateProvider.state('gk.rds',
        {
            url: '/rds',
            template: require('./component/rds/template/rds.tpl.html'),
            controller: 'gkRdsController',
            controllerAs: 'gkRdsCtrl',
            params: {
                'selectedIndex':-1,
                'selectedView':'rds'
            },
            reload:true
        }
    );

    $stateProvider.state('gk.rds.requests',
        {
            url: '/requests',
            template: require('./component/shared/request/template/gatekeeperRequest.tpl.html'),
            controller: 'gkRdsRequestController',
            controllerAs: 'gkAppCtrl',
            params: {
                'userId':'unknown',
                'user':'unknown',
                'role':'unauthorized',
                'email':'unknown'
            },
            onEnter:confirmAccess
        }
    );

    $stateProvider.state('gk.rds.history',
        {
            url: '/history',
            template: require('./component/shared/request/template/gatekeeperRequest.tpl.html'),
            controller: 'gkRdsRequestHistoryController',
            controllerAs: 'gkAppCtrl',
            params: {
                'userId':'unknown',
                'user':'unknown',
                'role':'unauthorized',
                'email':'unknown'
            },
            onEnter:confirmAccess
        }
    );

    $stateProvider.state('gk.rds.selfservice',
        {
            url: '/selfservice',
            template: require('./component/rds/selfservice/template/rdsSelfService.tpl.html'),
            controller: 'gkRdsSelfServiceController',
            controllerAs: 'gkSelfServiceCtrl',
            params: {
                'userId':'unknown',
                'user':'unknown',
                'memberships':[],
                'approvalThreshold':[],
                'role':'unauthorized',
                'email':'unknown'
            },
            onEnter:confirmAccess
        }
    );

    $stateProvider.state('gk.rds.admin',
        {
            url: '/admin',
            template: require('./component/rds/admin/template/rdsAdmin.tpl.html'),
            controller: 'gkRdsAdminController',
            controllerAs: 'gkAdminCtrl',
            params: {
                'userId':'unknown',
                'user':'unknown',
                'role':'unauthorized',
                'email':'unknown'
            },
            onEnter:confirmAccess
        }
    );

    $stateProvider.state('gk.denied',
        {
            url: '/denied',
            template: require('./component/error/denied.tpl.html')
        }
    );

    $stateProvider.state('gk.error',
        {
            url: '/error',
            template: require('./component/error/error.tpl.html')
        }
    );
}

export default function config($mdDateLocaleProvider, $mdThemingProvider, $stateProvider, $urlRouterProvider) {
    require('./assets/Compute_AmazonEC2.svg');
    require('./assets/Database_AmazonRDS.svg');
    require('./assets/gatekeeper.svg');
    configureMaterial($mdDateLocaleProvider, $mdThemingProvider);
    configureRouting($stateProvider, $urlRouterProvider);
}