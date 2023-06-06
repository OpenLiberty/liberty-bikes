FROM icr.io/appcafe/open-liberty:full-java11-openj9-ubi
ADD --chown=1001:0 build/libs/player-service.war /config/dropins
ADD --chown=1001:0 build/libs/postgresql-*.jar /config/postgresql
COPY --chown=1001:0 src/main/liberty/config /config/
RUN printf 'httpPort=8081\n\
httpsPort=8444' > /config/bootstrap.properties
#RUN configure.sh

EXPOSE 8081 8444