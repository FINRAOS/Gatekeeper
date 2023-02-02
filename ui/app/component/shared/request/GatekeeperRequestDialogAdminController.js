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

import GatekeeperRequestDialogController from './GatekeeperRequestDialogController';

let dialog;
let VM;
let REQUEST = Symbol();
let ROOTSCOPE = Symbol();
let approverComments;

class GatekeeperRequestDialogAdminController extends GatekeeperRequestDialogController {
    constructor($rootScope, $mdDialog, $mdToast, gkRequestService, row){
        super($rootScope, $mdDialog, $mdToast, gkRequestService, row);
        VM = this;
        VM[REQUEST] = gkRequestService;
        VM[ROOTSCOPE] = $rootScope;

        this.readonly = false;
        dialog = $mdDialog;
        approverComments = VM.row.approverComments;
    }

    approveRequest(){
        let title = 'Confirm Request Approval';
        let message = 'Are you sure you would like to approve the request?';
        let button = 'Approve Request'
        
        let config = {
            clickOutsideToClose: true,
            title: 'Confirm Request Approval',
            template: require("./template/confirm.tpl.html"),
            parent: angular.element(document.body),
            multiple: true,
            skipHide: true,
            locals: {
                title: title,
                message: message,
                button: button
            },
            controller: ['$scope', '$mdDialog', 'title', 'message', 'button', function ($scope, $mdDialog, title, message, button) {
                $scope.title = title;
                $scope.message = message;
                $scope.button = button;
                $scope.cancel = function () {
                    $mdDialog.cancel();
                };
                $scope.action = function () {
                    dialog.hide();
                    VM.called = true;
                    VM[REQUEST].approve(VM.row).then((resp)=>{
                        let msg = "Request " + VM.row.id + " successfully granted!";
                        VM.toast(msg);
                        dialog.hide();
                        VM[ROOTSCOPE].$emit("requestsUpdated");
                    }).catch(()=>{
                        let msg = "There was an error while attempting to grant access for request " + VM.row.id;
                        VM.dialog(msg);
                        dialog.hide();
                    });              
                };
            }
            ]
        };
        return dialog.show(config)
    }

    rejectRequest(){
        let title = 'Confirm Request Rejection';
        let message = 'Are you sure you would like to reject the request?';
        let button = 'Reject Request'
        
        let config = {
            clickOutsideToClose: true,
            title: 'Confirm Request Approval',
            template: require("./template/confirm.tpl.html"),
            parent: angular.element(document.body),
            multiple: true,
            skipHide: true,
            locals: {
                title: title,
                message: message,
                button: button
            },
            controller: ['$scope', '$mdDialog', 'title', 'message', 'button', function ($scope, $mdDialog, title, message, button) {
                $scope.title = title;
                $scope.message = message;
                $scope.button = button;
                $scope.cancel = function () {
                    $mdDialog.cancel();
                };
                $scope.action = function () {
                    dialog.hide();
                    VM.called = true;
                    VM[REQUEST].reject(VM.row).then((resp)=>{
                        let msg = "Request " + VM.row.id + " successfully rejected!";
                        VM.toast(msg);
                        dialog.hide();
                        VM[ROOTSCOPE].$emit("requestsUpdated");
                    }).catch(()=>{
                        let msg = "There was an error while attempting to reject access for request " + VM.row.id;
                        VM.dialog(msg);
                        dialog.hide();
                    });           
                };
            }
            ]
        };
        return dialog.show(config)
    }

    checkForApproverComment(){
        return !VM.row.approverComments;
    }

    checkForUpdatedApproverComment(){
        return (VM.row.approverComments === approverComments) || !VM.row.approverComments;
    }

    updateApproverComment(){
        let title = 'Confirm Approver Comment Update';
        let message = 'Are you sure you would like to update the approver comment for this request?';
        let button = 'Update'
        
        let config = {
            clickOutsideToClose: true,
            title: 'Confirm Approver Comment Update',
            template: require("./template/confirm.tpl.html"),
            parent: angular.element(document.body),
            multiple: true,
            skipHide: true,
            locals: {
                title: title,
                message: message,
                button: button
            },
            controller: ['$scope', '$mdDialog', 'title', 'message', 'button', function ($scope, $mdDialog, title, message, button) {
                $scope.title = title;
                $scope.message = message;
                $scope.button = button;
                $scope.cancel = function () {
                    $mdDialog.cancel();
                };
                $scope.action = function () {
                    dialog.hide();
                    VM.called = true;
                    VM[REQUEST].updateRequest(VM.row).then((resp)=>{
                        let msg = "Request " + VM.row.id + " approver comment successfully updated!";
                        VM.toast(msg);
                        dialog.hide();
                        VM[ROOTSCOPE].$emit("requestsUpdated");
                    }).catch(()=>{
                        let msg = "There was an error while attempting to update approver comment for the request " + VM.row.id;
                        VM.dialog(msg);
                        dialog.hide();
                    });           
                };
            }
            ]
        };
        return dialog.show(config)
    }
}

export default GatekeeperRequestDialogAdminController