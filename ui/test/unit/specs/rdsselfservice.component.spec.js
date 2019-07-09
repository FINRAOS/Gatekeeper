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
import AccountDataService from '../../../app/component/shared/AccountDataService';
import RdsGrantDataService from '../../../app/component/shared/RdsGrantDataService';
import RdsConfigService from '../../../app/component/shared/RdsConfigService';

//Controllers
import md from 'angular-material';
import router from 'angular-ui-router';
import RdsSelfServiceController from '../../../app/component/rds/selfservice/RdsSelfServiceController';


describe('GateKeeper RDS SelfService component', function () {

    let $http, $state, $mdDialog, $mdToast, scope, controller;

    beforeEach(angular.mock.module(md, router));

    beforeEach(inject(function($rootScope, _$state_, _$http_,_$mdDialog_,_$mdToast_){
        scope = $rootScope.$new();
        $state =_$state_;
        $http = _$http_;
        $mdDialog = _$mdDialog_;
        $mdToast = _$mdToast_;
        $state.current.name = 'gatekeeper.rds.admin';
    }));

    //mock all this stuff out.
    let $q, $rootScope, $httpBackend;
    let gkAccountService, gkRdsConfigService, gkRdsGrantService;

    describe('RdsSelfServiceController', function(){
        beforeEach(inject(function(_$q_, _$rootScope_, _$httpBackend_){
            $q = _$q_;
            $rootScope = _$rootScope_;
            $httpBackend = _$httpBackend_;

            gkRdsGrantService = new RdsGrantDataService($http, $state);
            gkRdsConfigService = new RdsConfigService($http, $state);
            gkAccountService = new AccountDataService($http, $state);

        }));

        let testInit = (happy) => {
            let deferred = $q.defer();
            spyOn(gkAccountService, 'fetch').and.returnValue(deferred.promise);
            let resp = {data:['stuff']};


            $rootScope.userInfo = {
                userId:'testId',
                user:'test',
                email:'test@email.com',
                roleMemberships: {
                    APP: {
                        roles: {
                            DEV: [
                                'DEV',
                                'QA'
                            ]
                        }
                    },
                    APP2: {
                        roles: {
                            DEV: [
                                'DEV'
                            ]
                        }
                    }
                }
            };

            $rootScope.rdsMaxDays = 100;
            $rootScope.rdsOverridePolicy = {
                APP: {
                    appSpecificOverridePolicy: {
                        DATAFIX: {
                            prod: 10
                        },
                        DBA: {
                            prod: 5
                        },
                        DBA_CONFIDENTIAL: {
                            prod: 2
                        },
                        READONLY_CONFIDENTIAL: {
                            prod: 4
                        }
                    }
                },
                APP2: {
                    appSpecificOverridePolicy: {
                        DATAFIX: {
                            prod: 10
                        },
                        DBA: {
                            prod: 5
                        },
                        DBA_CONFIDENTIAL: {
                            prod: 2
                        },
                        READONLY_CONFIDENTIAL: {
                            prod: 4
                        }
                    }
                },
                APP3: {
                    appSpecificOverridePolicy: {
                        DATAFIX: {
                            prod: 10
                        },
                        DBA: {
                            prod: 5
                        },
                        DBA_CONFIDENTIAL: {
                            prod: 2
                        },
                        READONLY_CONFIDENTIAL: {
                            prod: 4
                        }
                    }
                }
            };

            $rootScope.userInfo.approvalThreshold = {
                APP: {
                    appSpecificApprovalThresholds: {
                        DATAFIX: {
                            dev: 99,
                            qa: 50,
                            prod: -1
                        },
                        DBA: {
                            dev: 76,
                            qa: 50,
                            prod: -1
                        },
                        READONLY: {
                            dev: 65,
                            qa: 50,
                            prod: -1
                        },
                        READONLY_CONFIDENTIAL: {
                            dev: 99,
                            qa: 50,
                            prod: -1
                        },
                        DBA_CONFIDENTIAL: {
                            dev: 99,
                            qa: 50,
                            prod: -1
                        }
                    }
                },
                APP2: {
                    appSpecificApprovalThresholds: {
                        DATAFIX: {
                            dev: 99,
                            qa: -1,
                            prod: -1
                        },
                        DBA: {
                            dev: 76,
                            qa: -1,
                            prod: -1
                        },
                        READONLY: {
                            dev: 65,
                            qa: -1,
                            prod: -1
                        },
                        READONLY_CONFIDENTIAL: {
                            dev: 99,
                            qa: -1,
                            prod: -1
                        },
                        DBA_CONFIDENTIAL: {
                            dev: 99,
                            qa: -1,
                            prod: -1
                        }
                    }
                },
                APP3: {
                    appSpecificApprovalThresholds: {
                        DATAFIX: {
                            dev: -1,
                            qa: -1,
                            prod: -1
                        },
                        DBA: {
                            dev: -1,
                            qa: -1,
                            prod: -1
                        },
                        READONLY: {
                            dev: -1,
                            qa: -1,
                            prod: -1
                        },
                        READONLY_CONFIDENTIAL: {
                            dev: -1,
                            qa: -1,
                            prod: -1
                        },
                        DBA_CONFIDENTIAL: {
                            dev: -1,
                            qa: -1,
                            prod: -1
                        }
                    }
                }
            };


            if(happy) {
                deferred.resolve(resp);
            }else{
				
                deferred.reject(resp);
            }

            controller = new RdsSelfServiceController($mdDialog, $mdToast, gkAccountService, gkRdsGrantService, gkRdsConfigService, scope, $state, $rootScope);
            controller.forms.awsInstanceForm = {
                selectedAccount: {},
            };
            controller.forms.grantForm = {
                selectedRoles: {},
            };
        };

        it('Should Initialize properly', function(){
            testInit();
        });

        describe('Test disableRoleCheckbox() method', () => {
            beforeEach(() => {
                testInit();
            });

            it('should disable if all dbs do not contain the role', () => {
                controller.forms.awsInstanceForm.selectedAccount.sdlc = 'dev';
                controller.selectedItems = [
                    {
                        application: 'APP',
                        availableRoles: ['gk_readonly', 'gk_dba', 'gk_datafix']
                    },
                    {
                        application: 'APP2',
                        availableRoles: ['gk_readonly', 'gk_dba', 'gk_datafix', 'gk_readonly_confidential', 'gk_dba_confidential']
                    },
                ];
                controller.forms.grantForm.selectedRoles = {
                    datafix: true,
                    readonly: false
                };

                let gkReadOnlyConfidentialDisableFn = controller.disableRoleCheckbox('gk_readonly_confidential');

                let result = gkReadOnlyConfidentialDisableFn();
                expect(result).toBeTruthy();

            });

            it('should enable if both dbs have the role', () => {
                controller.forms.awsInstanceForm.selectedAccount.sdlc = 'dev';
                controller.selectedItems = [
                    {
                        application: 'APP',
                        availableRoles: ['gk_readonly', 'gk_dba', 'gk_datafix']
                    },
                    {
                        application: 'APP2',
                        availableRoles: ['gk_readonly', 'gk_dba', 'gk_datafix', 'gk_readonly_confidential', 'gk_dba_confidential']
                    },
                ];
                controller.forms.grantForm.selectedRoles = {
                    datafix: true,
                    readonly: false
                };

                let gkReadOnlyConfidentialDisableFn = controller.disableRoleCheckbox('gk_readonly');

                let result = gkReadOnlyConfidentialDisableFn();
                expect(result).toBeFalsy();

            });

            it('should disable if no items are selected', () => {
                controller.forms.awsInstanceForm.selectedAccount.sdlc = 'dev';
                controller.selectedItems = [

                ];
                controller.forms.grantForm.selectedRoles = {
                    datafix: true,
                    readonly: false
                };

                let gkReadOnlyConfidentialDisableFn = controller.disableRoleCheckbox('gk_readonly');

                let result = gkReadOnlyConfidentialDisableFn();
                expect(result).toBeTruthy();

            });
        });

        describe('Test getApprovalBounds()', ()=> {
            beforeEach(() => {
                testInit();
            });

            it('should return maximum days if the user is an approver', () => {
              $rootScope.userInfo.isApprover = true;
              let bound = controller.getApprovalBounds();
              expect(bound).toEqual(100);
            });

            it('should return specific days if the user is a non-approver and has the same sdlc/application permissions', () => {
                controller.forms.awsInstanceForm.selectedAccount.sdlc = 'dev';
                controller.selectedItems = [
                    {
                        application: 'APP'
                    },
                    {
                        application: 'APP2'
                    },
                ];
                controller.forms.grantForm.selectedRoles = {
                    datafix: true,
                    readonly: false
                };
                let bound = controller.getApprovalBounds();
                expect(bound).toEqual(99);

                controller.forms.grantForm.selectedRoles = {
                    datafix: false,
                    readonly: true
                };
                bound = controller.getApprovalBounds();
                expect(bound).toEqual(65);

            });

            it('should return -1 if the user is a does not have the correct application membership', () => {
                controller.forms.awsInstanceForm.selectedAccount.sdlc = 'dev';
                controller.selectedItems = [
                    {
                        application: 'APP'
                    },
                    {
                        application: 'APP3'
                    }
                ];
                controller.forms.grantForm.selectedRoles = {
                    readonly: true
                };

                let bound = controller.getApprovalBounds();
                expect(bound).toEqual(-1);

            });


            it('should return -1 if the user is a does not have the correct SDLC membership', () => {
                controller.forms.awsInstanceForm.selectedAccount.sdlc = 'prod';
                controller.selectedItems = [
                    {
                        application: 'APP'
                    },
                ];
                controller.forms.grantForm.selectedRoles = {
                    readonly: true
                };

                let bound = controller.getApprovalBounds();
                expect(bound).toEqual(-1);

            });

        });

        describe('Test getMaximumDays()', ()=> {
            beforeEach(() => {
                testInit();
            });

            it('Should default to maximum days if no override for a given role is found', () => {
               controller.forms.awsInstanceForm.selectedAccount = {
                   sdlc: 'dev'
               };

               controller.forms.grantForm.selectedRoles = {
                   readonly: true
               };

               let max = controller.getMaximumDays();

               expect(max).toEqual(100);
           });

            it('Should be set to 10 if prod sdlc and datafix role is selected', () => {
                controller.forms.awsInstanceForm.selectedAccount = {
                    sdlc: 'prod'
                };

                controller.selectedItems = [
                    {
                        application: 'APP'
                    },
                    {
                        application: 'APP2'
                    },
                ];

                controller.forms.grantForm.selectedRoles = {
                    datafix: true,
                    readonly: false
                };

                let max = controller.getMaximumDays();

                expect(max).toEqual(10);
            });

            it('Should be set to 4 if prod sdlc and datafix + readonly_confidential role is selected', () => {
                controller.forms.awsInstanceForm.selectedAccount = {
                    sdlc: 'prod'
                };
                controller.selectedItems = [
                    {
                        application: 'APP'
                    },
                    {
                        application: 'APP2'
                    },
                ];

                controller.forms.grantForm.selectedRoles = {
                    datafix: true,
                    readonly_confidential: true,
                    readonly: true
                };

                let max = controller.getMaximumDays();

                expect(max).toEqual(4);
            });

        });
    });
});
