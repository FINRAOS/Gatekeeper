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

import { parse } from 'json2csv';
import { saveAs } from 'file-saver';
import Directive from '../utils/BaseDirective';

/**
 * A simple wrapper around md-data-table   
 */
class BaseTable extends Directive{
    constructor(template){
        super(template);
        require('angular-material-data-table/dist/md-data-table.min.css');
        this.scope = {
            config: '='
        };
        this.controllerAs = 'ctrl';
    }

    controller($scope) {
        this.export = () => {
            const { filename = 'data', fields } = $scope.config.export;
            const blob = new Blob([parse($scope.config.data, { fields })], { type: 'text/csv;charset=utf-8' });
            saveAs(blob, `${filename}.csv`);
        }
    }
}

export default BaseTable;



