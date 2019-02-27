FROM open-liberty:microProfile1
ADD build/libs/player-service.war /config/dropins
COPY src/main/liberty/config /config/
RUN printf 'httpPort=8081\n\
httpsPort=8444' > /config/bootstrap.properties

EXPOSE 8081 8444