FROM websphere-liberty:beta
ADD build/libs/game-service.zip /
RUN unzip game-service.zip
RUN echo "-Dorg.libertybikes.restclient.PlayerService/mp-rest/url=\
http://lb-player:8081/" > wlp/usr/servers/game-service/jvm.options
RUN mv wlp/usr/servers/game-service/* /config/
RUN rm -rf wlp/