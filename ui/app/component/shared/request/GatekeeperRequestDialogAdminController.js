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

class GatekeeperRequestDialogAdminController extends GatekeeperRequestDialogController {
    constructor($rootScope, $mdDialog, $mdToast, gkRequestService, row){
        super($rootScope, $mdDialog, $mdToast, gkRequestService, row);
        VM = this;
        VM[REQUEST] = gkRequestService;
        VM[ROOTSCOPE] = $rootScope;

        this.readonly = false;
        dialog = $mdDialog;

    }

    approveRequest(){
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
    }

    rejectRequest(){
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
    }
}

export default GatekeeperRequestDialogAdminController