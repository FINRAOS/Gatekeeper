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

import DataService from './generic/DataService';

/**
 * This service grants access to users
 * @returns {{}}
 */

class GrantDataService extends DataService{
    constructor($http,$state){
        super($http,$state);
        this.resource = 'grantAccess';
    }

    post(roles, days, users, account, accountSdlc, region, instances, ticketId, requestReason, platform){
        var bundle = {
            roles: roles,
            account: account,
            accountSdlc: accountSdlc,
            region: region,
            days: days,
            users: users,
            instances: instances,
            ticketId: ticketId,
            requestReason: requestReason,
            platform: platform
        };

        return this.http.post(this.getApi()+'/'+this.resource, bundle);
    }
}

export default GrantDataService;
