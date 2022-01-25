/*
 * Copyright 2022. Gatekeeper Contributors
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

package org.finra.gatekeeper.services.accessrequest.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Repo object for AccessRequest domain
 */
public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {
    AccessRequest getAccessRequestById(Long id);
    List<AccessRequest> getAccessRequestsByIdIn(Collection<Long> ids);

    /**
     * Gets all live access requests by joining all of the entries into the act_ru_job tables (activiti jobs that monitor expiration)
     * into the access request table
     *
     * @return - AccessRequest objects that are linked to the entry in the act_ru_job table
     */
    @Query(value = "select requests.* from ( " +
            "                  select c.id, b.updated as granted_on, a.duedate_ as expire_time " +
            "                  from (select process_instance_id_, duedate_ " +
            "                        from gatekeeper_rds.act_ru_job " +
            "                        where type_ = 'timer') a, " +
            "                       (select proc_inst_id_, text2_ as access_req_id, last_updated_time_ as updated " +
            "                        from gatekeeper_rds.act_hi_varinst " +
            "                        where name_ = 'accessRequest') b, " +
            "                       gatekeeper_rds.access_request c " +
            " " +
            "                  where a.process_instance_id_ = b.proc_inst_id_ " +
            "                    and cast(b.access_req_id as numeric) = c.id " +
            "                  order by access_req_id desc " +
            "              ) live_requests, " +
            "              gatekeeper_rds.access_request requests " +
            "where requests.id = live_requests.id", nativeQuery = true)
    List<AccessRequest> getLiveAccessRequests();

    @Query(value = "select requests.*, live_requests.granted_on, live_requests.expire_time from (  " +
            "                  select c.id, b.updated as granted_on, a.duedate_ as expire_time  " +
            "                  from (select process_instance_id_, duedate_  " +
            "                        from gatekeeper_rds.act_ru_job  " +
            "                        where type_ = 'timer') a,  " +
            "                       (select proc_inst_id_, text2_ as access_req_id, last_updated_time_ as updated  " +
            "                            from gatekeeper_rds.act_hi_varinst  " +
            "                            where name_ = 'accessRequest') b,  " +
            "                       (select * from gatekeeper_rds.access_request requests  " +
            "                            where account = :account) c,  " +
            "                       gatekeeper_rds.access_request_users d,  " +
            "                       (select * from gatekeeper_rds.request_user users  " +
            "                            where users.user_id = :username) e,  " +
            "                       gatekeeper_rds.access_request_aws_rds_instances f,  " +
            "                       (select * from gatekeeper_rds.request_database dbs  " +
            "                            where dbs.name = :dbname) g,  " +
            "                       gatekeeper_rds.access_request_roles i,  " +
            "                       (select * from gatekeeper_rds.request_role roles  " +
            "                           where roles.role = :role) h  " +
            "  " +
            "                  where a.process_instance_id_ = b.proc_inst_id_  " +
            "                    and cast(b.access_req_id as numeric) = c.id  " +
            "                    and c.id = d.access_request_id  " +
            "                    and d.users_id = e.id  " +
            "                    and c.id = f.access_request_id  " +
            "                    and f.aws_rds_instances_id = g.id  " +
            "                    and c.id = i.access_request_id  " +
            "                    and i.roles_id = h.id  " +
            "                  order by access_req_id desc  " +
            "              ) live_requests,  " +
            "              gatekeeper_rds.access_request requests  " +
            "where requests.id = live_requests.id", nativeQuery = true)
    List<AccessRequest> getLiveAccessRequestsForUserAccountDbNameAndRole(@Param("username") String username,
                                                                         @Param("account") String account,
                                                                         @Param("dbname") String dbName,
                                                                         @Param("role") String role);

    /**
     * This gets all of the Expiration metadata related to the given access request. T
     * @return a Map<String,Object> containing the following metadata: the ID of the access request, the time the request was granted
     * and the time the request will expire.
     */
    @Query(value = "select requests.id, live_requests.granted_on, live_requests.expire_time from ( " +
            "                  select c.id, b.updated as granted_on, a.duedate_ as expire_time " +
            "                  from (select process_instance_id_, duedate_ " +
            "                        from gatekeeper_rds.act_ru_job " +
            "                        where type_ = 'timer') a, " +
            "                       (select proc_inst_id_, text2_ as access_req_id, last_updated_time_ as updated " +
            "                        from gatekeeper_rds.act_hi_varinst " +
            "                        where name_ = 'accessRequest') b, " +
            "                       gatekeeper_rds.access_request c " +
            " " +
            "                  where a.process_instance_id_ = b.proc_inst_id_ " +
            "                    and cast(b.access_req_id as numeric) = c.id " +
            "                  order by access_req_id desc " +
            "              ) live_requests, " +
            "              gatekeeper_rds.access_request requests " +
            "where requests.id = live_requests.id", nativeQuery = true)
    List<Map<String, Object>> getLiveAccessRequestExpirations();
}
