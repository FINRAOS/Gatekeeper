FROM gatekeeper/java:latest
MAINTAINER Gatekeeper Contributors

ARG jar_file
ADD target/${jar_file} /usr/share/gatekeeper/app.jar
RUN ls -ltr /usr/share/gatekeeper
ENV JAVA_OPTS=""
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=container","-jar","/usr/share/gatekeeper/app.jar"]