FROM icr.io/appcafe/open-liberty:kernel-slim-java11-openj9-ubi

# Pick up latest security fixes
USER root
RUN yum -y update && \
    yum clean all && \
    rm -rf /var/cache/yum /tmp/* /var/tmp/*
USER 1001

ADD --chown=1001:0 build/libs/game-service.war /config/dropins
COPY --chown=1001:0 src/main/liberty/config /config/
RUN printf 'httpPort=8080\n\
httpsPort=8443' > /config/bootstrap.properties
#RUN printf -- "-Dorg.libertybikes.restclient.PlayerService/mp-rest/url=\
#http://lb-player:8081/" > /config/jvm.options

# Install necessary features
RUN features.sh

EXPOSE 8080 8443

# This script will add the requested XML snippets, grow image to be fit-for-purpose and apply interim fixes
# We disable the SCC because this modifies the class files and breaks code coverage during the build process
ENV OPENJ9_SCC=false
RUN configure.sh

WORKDIR /opt/ol/wlp/usr/servers/defaultServer