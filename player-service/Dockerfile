FROM open-liberty:19.0.0.9-microProfile3-java11
ADD --chown=1001:0 build/libs/player-service.war /config/dropins
ADD --chown=1001:0 build/libs/postgresql-*.jar /config/postgresql
COPY --chown=1001:0 src/main/liberty/config /config/
RUN printf 'httpPort=8081\n\
httpsPort=8444' > /config/bootstrap.properties

EXPOSE 8081 8444