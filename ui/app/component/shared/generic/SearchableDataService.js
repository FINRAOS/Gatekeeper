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

import DataService from './DataService';

/**
 * Generic Data Service for gatekeeper
 */
class SearchableDataService extends DataService{
    constructor($http,$state){
        super($http,$state);
        this.params = [];
    }

    /**
     * GET request with a search string
     * @returns {*}
     */
    search(searchParams){
        if(typeof searchParams !== 'object' || !angular.equals(this.params.length, Object.keys(searchParams).length)){
            throw 'SEARCH: searchParams must be object containing the following values ' + this.params;
        }

        var url =  this.getApi()+'/'+this.resource + '?';
        this.params.forEach((item, index) =>{
            url = url + item + '=' + (searchParams[item] ? searchParams[item] : '');
            if(index !== this.params.length - 1){
                url += '&';
            }
        });

        return this.http.get(url);
    }
}

export default SearchableDataService;