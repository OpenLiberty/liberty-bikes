FROM websphere-liberty:beta
ADD build/libs/player-service.zip /
RUN unzip player-service.zip
RUN mv wlp/usr/servers/player-service/* /config/
RUN rm -rf wlp/

EXPOSE 8081