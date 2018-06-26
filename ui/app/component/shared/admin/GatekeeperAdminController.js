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

import GatekeeperSubmissionDialogController from "../selfservice/GatekeeperSubmissionDialogController";

let vm;

const DIALOG = Symbol();
const TOAST = Symbol();

class GatekeeperAdminController {
    constructor($mdDialog, $mdToast){
        vm = this;
        vm[DIALOG] = $mdDialog;
        vm[TOAST] = $mdToast;
        vm.forms = {};
        vm.error = {};
    }
    
    spawnConfirmDialog(title, message){
        let confirm = this[DIALOG].confirm()
            .hasBackdrop(true)
            .title(title)
            .content(message)
            .ok('Yes')
            .cancel('No');
        return this[DIALOG].show(confirm);
    }

    spawnAlertDialog(title, message){
        let confirm = this[DIALOG].alert()
            .hasBackdrop(true)
            .title(title)
            .content(message)
            .ok('Dismiss');
        return this[DIALOG].show(confirm);
    }
}

export default GatekeeperAdminController;



