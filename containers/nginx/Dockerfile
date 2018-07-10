#
#  Copyright 2018. Gatekeeper Contributors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

FROM gatekeeper/base:latest
MAINTAINER Gatekeeper Contributors

ARG proxy
ENV http_proxy ${proxy}
ENV https_proxy ${proxy}

RUN amazon-linux-extras install nginx1.12

RUN mkdir -p /opt/gatekeeper/static
ADD static/index.html /opt/gatekeeper/static/index.html
ADD vhost/nginx.conf /etc/nginx/nginx.conf

ENV http_proxy ""
ENV https_proxy ""

CMD ["nginx", "-g", "daemon off;"]