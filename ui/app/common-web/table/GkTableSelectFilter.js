
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

/**
 * A filter that works off a collection of defined checkbox objects
 */
class PetTableSelectFilter{
    constructor(input, filters){
        this.input = input;
        this.filters = filters
    }

    /**
     * This is where the filtering happens
     * @returns {*}
     */
    execute() {
        if(!this.filters){
            return this.input;
        }
        
        let result = [];
        
        //For each data item lets check that all the filters match, currently it is intersection based.
        this.input.forEach((item) =>{
            let match = true;
            this.filters.forEach((filter) => {
                //if it matches and the filter is checked check against the other filters
                if(match && filter.checked) {
                    match = filter.checked === 'ALL' || filter.filterFn(filter.checked, item);
                }
            });
            
            //if match is still true, this item meets the filter criteria so add it to the result set
            if(match){
                result.push(item);
            }
        });

        return result;
    }
    
    static GkTableSelectFilterFactory(input, filters){
        let filter = new PetTableSelectFilter(input, filters);
        return filter.execute();
    }
}

export default PetTableSelectFilter;
