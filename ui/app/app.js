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

import md from 'angular-material';
import state from 'angular-ui-router';
import gk from './component/gatekeeper';
import select from './component/select';
import gkEc2 from './component/ec2';
import gkRds from './component/rds';
import 'angular-material/angular-material.min.css';
import Config from './app.config.js';
import runBase from './app.run.js';
import commonWeb from './common-web';

//This is for testing with mocked Rest Responses.
//import runMocked from '../test/app.run.mocked';
//cause I have no idea how to get 'ngMockE2E' this new way.... ;)
angular.module('app', [md, state, gk, commonWeb, gkEc2, gkRds, select])
    .config(Config);

