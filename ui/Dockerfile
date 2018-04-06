FROM gatekeeper/nginx:latest
MAINTAINER Gatekeeper Contributors

ADD dist/* /opt/gatekeeper/static/
ADD dist/assets /opt/gatekeeper/static/assets
RUN echo "I am healthy" > /opt/gatekeeper/static/health.html