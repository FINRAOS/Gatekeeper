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

const AD = Symbol();
const DIALOG = Symbol();
const TOAST = Symbol();
const RDS = Symbol();
const GRANT = Symbol();
let STATE = Symbol();

//need this to deal with callbacks
let vm;

class RdsSchemaDialogController {
    constructor($mdDialog, gkSchemaService, database, account, region){

        vm = this;

        vm.database = database;
        this[DIALOG] = $mdDialog;

        vm.fetched = false;
        vm.schemaPromise = gkSchemaService.search({account:account.alias, region: region.name, instanceName: database.name, instanceId: database.instanceId});
        vm.schemaPromise.then((response) => {
            vm.schemas = response.data;
            vm.fetched = true;
        });


    }

    closeDialog(){
        vm[DIALOG].hide();
    }

}

export default RdsSchemaDialogController;
