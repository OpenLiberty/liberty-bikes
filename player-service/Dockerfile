FROM icr.io/appcafe/open-liberty:kernel-slim-java11-openj9-ubi

ARG VERSION_LABEL=0.0.1
ARG RELEASE_LABEL=0.0.0
ARG VCS_REF=0123456789012345678901234567890123456789
ARG VCS_URL="https://github.ibm.com/OpenLiberty/liberty-bikes"
ARG NAME="liberty-bikes-player-service"
ARG SUMMARY="Player microservice for Liberty Bikes"
ARG DESCRIPTION="Provides player services for Liberty Bikes."

LABEL name=$NAME \
      vendor=IBM \
      version=$VERSION_LABEL \
      release=$RELEASE_LABEL \
      description=$DESCRIPTION \
      summary=$SUMMARY \
      io.k8s.display-name=$SUMMARY \
      io.k8s.description=$DESCRIPTION \
      vcs-type=git \
      vcs-ref=$VCS_REF \
      vcs-url=$VCS_URL \
      url=$VCS_URL

# Pick up latest security fixes
USER root
RUN yum -y update && \
    yum clean all && \
    rm -rf /var/cache/yum /tmp/* /var/tmp/*
USER 1001

# Add app and config
COPY --chown=1001:0 build/libs/player-service.war /config/dropins
COPY --chown=1001:0 build/libs/postgresql-*.jar /config/postgresql
COPY --chown=1001:0 src/main/liberty/config/server.xml /config/
COPY --chown=1001:0 src/main/liberty/config/bootstrap.properties /config/
COPY --chown=1001:0 src/main/liberty/config/resources/security/validationKeystore.jks /config/resources/security/

# Install necessary features
RUN features.sh

# Endpoints
EXPOSE 8081 8444

# This script will add the requested XML snippets, grow image to be fit-for-purpose and apply interim fixes
# We disable the SCC because this modifies the class files and breaks code coverage during the build process
ENV OPENJ9_SCC=false
RUN configure.sh

WORKDIR /opt/ol/wlp/usr/servers/defaultServer
