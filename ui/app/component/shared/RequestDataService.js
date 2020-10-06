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

class RequestDataService extends DataService{
    constructor($http,$state){
        super($http,$state);
        this.getActiveRequests = 'getActiveRequests';
        this.getCompletedRequests = 'getCompletedRequests';
        this.approveRequest = 'approveRequest';
        this.rejectRequest = 'rejectRequest';
        this.cancelRequest = 'cancelRequest';
        this.getRequests = 'requests';
    }

    getActive(){
        return this.http.get(this.getApi()+'/'+this.getActiveRequests);
    }
    
    getCompleted(){
        return this.http.get(this.getApi()+'/'+this.getCompletedRequests);
    }

    getRequest(request){
        return this.http.get(this.getApi()+'/'+this.getRequests+'/'+request);
    }

    approve(request){
        return this.http.put(this.getApi()+"/"+this.approveRequest, request);
    }

    reject(request){
        return this.http.put(this.getApi()+"/"+this.rejectRequest, request);
    }
    
    cancel(request){
        return this.http.put(this.getApi()+"/"+this.cancelRequest, request);
    }

}

export default RequestDataService;