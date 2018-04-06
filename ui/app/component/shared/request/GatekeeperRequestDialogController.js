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

let dialog;
let REQUEST = Symbol();
let TOAST = Symbol();
let ROOTSCOPE = Symbol();
let VM;

class GatekeeperRequestDialogController{
    constructor($rootScope, $mdDialog, $mdToast, gkRequestService, row ){
        VM = this;

        let close = {
            label:'Close',
            action:this.closeDialog,
            style:"md-primary"
        };

        this.cancel = {
            label: 'Cancel Request',
            action: this.cancelRequest,
            style: 'md-raised md-primary'
        };
        
        this.actions = [close];
        this.row = row;
        this.readonly = true;
        dialog = $mdDialog;
        this[REQUEST] = gkRequestService;
        this[TOAST] =  $mdToast;
        this[ROOTSCOPE] = $rootScope;
    }

    toast(message){
        VM[TOAST].show(VM[TOAST].simple().textContent(message)
            .parent("#gkRequestContainer")
            .position('top right')
            .hideDelay(5000)
        );
    }

    cancelRequest(){
        VM.called = true;
        VM[REQUEST].cancel(VM.row).then((resp)=>{
            let msg = "Request " + VM.row.id + " has been successfully canceled!";
            VM.toast(msg);
            dialog.hide();
            VM[ROOTSCOPE].$emit("requestsUpdated");
        }).catch(()=>{
            let msg = "There was an error while attempting to cancel access request " + VM.row.id;
            VM.toast(msg);
            dialog.hide();
        });
    }

    closeDialog(){
        dialog.hide();
    }
}

export default GatekeeperRequestDialogController;