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

import util from '../utils/DirectiveUtils';

import GkTableCheckboxFilter from './GkTableCheckboxFilter';
import GkTableSelectFilter from './GkTableSelectFilter';

import BaseTable from './BaseTable';
import GkTableConfig from './GkTableConfig';
import GkDialogTableConfig from './GkDialogTableConfig';
import GkTableDataConfig from './GkTableDataConfig';

import md from 'angular-material';
import table from 'angular-material-data-table';

require('./assets/gk-table.css');

export default angular.module('gk-tables', [md, table])
    .directive('gkTable', util.newDirective(new BaseTable(require('./templates/gkTable.tpl.html'))))
    .directive('gkTableReadOnly', util.newDirective(new GkTableConfig(require('./templates/gkTableReadOnly.tpl.html'))))
    .directive('gkTableSingleSelect', util.newDirective(new GkTableConfig(require('./templates/gkTableSingleSelect.tpl.html'))))
    .directive('gkTableMultiSelect', util.newDirective(new GkTableConfig(require('./templates/gkTableMultiSelect.tpl.html'))))
    .directive('gkTableDialog', util.newDirective(new GkDialogTableConfig(require('./templates/gkTableSingleSelectDialog.tpl.html'))))
    .directive('gkTableData', util.newDirective(new GkTableDataConfig(require('./templates/gkTableData.tpl.html'))))
    .directive('gkTableDataString', util.newDirective(new GkTableDataConfig(require('./templates/gkTableDataString.tpl.html'))))
    .directive('gkTableDataNumber',  util.newDirective(new GkTableDataConfig(require('./templates/gkTableDataNumber.tpl.html'))))
    .directive('gkTableDataDate', util.newDirective(new GkTableDataConfig(require('./templates/gkTableDataDate.tpl.html'))))
    .filter('gkTableCheckboxFilter', () => GkTableCheckboxFilter.GkTableCheckboxFilterFactory)
    .filter('gkTableSelectFilter', () => GkTableSelectFilter.GkTableSelectFilterFactory).name;

