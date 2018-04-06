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

import table from  "./table";
import banner from "./banner";
import list from "./list";

import 'angular-material/angular-material.min.css';

export default angular.module('gk-common-web', [banner, table, list]).config(($mdIconProvider)=>{
    $mdIconProvider
        .iconSet('action', require('./assets/action-icons.svg'), 24)
        .iconSet('alert', require('./assets/alert-icons.svg'), 24)
        .iconSet('av', require('./assets/av-icons.svg'), 24)
        .iconSet('communication', require('./assets/communication-icons.svg'), 24)
        .iconSet('content', require('./assets/content-icons.svg'), 24)
        .iconSet('device', require('./assets/device-icons.svg'), 24)
        .iconSet('editor', require('./assets/editor-icons.svg'), 24)
        .iconSet('file', require('./assets/file-icons.svg'), 24)
        .iconSet('hardware', require('./assets/hardware-icons.svg'), 24)
        .iconSet('image', require('./assets/image-icons.svg'), 24)
        .iconSet('maps', require('./assets/maps-icons.svg'), 24)
        .iconSet('navigation', require('./assets/navigation-icons.svg'), 24)
        .iconSet('notification', require('./assets/notification-icons.svg'), 24)
        .iconSet('places', require('./assets/places-icons.svg'), 24)
        .iconSet('social', require('./assets/social-icons.svg'), 24)
        .iconSet('toggle', require('./assets/toggle-icons.svg'), 24);
}).name;