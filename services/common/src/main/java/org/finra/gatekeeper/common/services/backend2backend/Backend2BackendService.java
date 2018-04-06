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

package org.finra.gatekeeper.common.services.backend2backend;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Service which does backend calls
 */

@Component
public class Backend2BackendService {

    private String url(String url, String uri, boolean https){
        url += "/" + uri;
        //if the url has https:// or http:// already in it, nothing needs to be done
        if(url.contains("https://") || url.contains("http://")){ ;
            return url;
        }
        return (https ? "https://" : "http://") + url;
    }

    public <T> T makeGetCall(String url, String service, boolean useHttps, Class<T> clazz){
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url(url, service, useHttps), clazz);
    }

    public <T> ResponseEntity<T> makeGetCallWithResponse(String url, String service, boolean useHttps, Class<T> clazz){
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity(url(url, service, useHttps), clazz);
    }

    public <T,E> T makePostCall(String url, String service, E requestObject, boolean useHttps, Class<T> clazz){
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(url(url, service, useHttps), requestObject, clazz);
    }

    public <T,E> ResponseEntity<T> makePostCallWithResponse(String url, String service, E requestObject, boolean useHttps, Class<T> clazz){
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForEntity(url(url, service, useHttps), requestObject, clazz);
    }
}

