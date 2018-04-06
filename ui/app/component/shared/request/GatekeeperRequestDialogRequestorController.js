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
let REQUEST = Symbol();
let STATE = Symbol();
let TOAST = Symbol();
let ROOTSCOPE = Symbol();
let VM;

class GatekeeperRequestDialogRequestorController extends GatekeeperRequestDialogController{
    constructor($rootScope, $mdDialog, $mdToast, gkRequestService, row){
        super($rootScope, $mdDialog, $mdToast, gkRequestService, row);
        VM = this;
        
        this.actions.unshift(this.cancel);
        this.row = row;
        this.readonly = true;
        dialog = $mdDialog;
        this[REQUEST] = gkRequestService;
        this[TOAST] =  $mdToast;
        this[ROOTSCOPE] = $rootScope;
    }
}

export default GatekeeperRequestDialogRequestorController;