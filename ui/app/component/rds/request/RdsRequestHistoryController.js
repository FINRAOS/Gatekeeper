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

import GatekeeperRequestHistoryController from '../../shared/request/GatekeeperRequestHistoryController';

class RdsRequestHistoryController extends GatekeeperRequestHistoryController{
    constructor(gkRequestService){
        super(gkRequestService);

        this.requestTable.template = require('./template/request.tpl.html');

        this.requestTable.headers = [
            {dataType:'date',  config:{dateFormat:'short'}, display:'Created', value:'created'},
            {dataType:'date',  config:{dateFormat:'short'}, display:'Updated', value:'updated'},
            {dataType:'string',display:'Request ID', value:'id'},
            {dataType:'string',display:'Environment/Account', value: 'account'},
            {dataType:'string',display:'Requestor Name', value:'requestorName'},
            {dataType:'string',display:'Requestor Email', value:'requestorEmail'},
            {dataType:'number',display:'Days Requested', value:'days'},
            {dataType:'number',display:'Users', value:'userCount'},
            {dataType:'string',display:'Outcome', value:'status'}
        ];

        this.getCompleted();
    }
}

export default RdsRequestHistoryController;



