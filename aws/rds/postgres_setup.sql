/*
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
 *
 */

 create user gatekeeper with password 'YOU_PROVIDE_THIS';
alter role gatekeeper with CREATEROLE;
create role gk_readonly;
create role gk_dba;
create role gk_datafix;
create role gk_readonly_confidential;
create fole gk_dba_confidential;

--we have to grant all roles to gatekeeper for the schema query
grant gk_readonly to gatekeeper;
grant gk_datafix to gatekeeper;
grant gk_dba to gatekeeper;
grant gk_readonly_confidential to gatekeeper;
grant gk_dba_confidential to gatekeeper;