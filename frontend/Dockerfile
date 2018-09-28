FROM websphere-liberty:beta
ADD build/libs/frontend.zip /
RUN unzip frontend.zip
RUN mv wlp/usr/servers/frontendServer/* /config/
RUN rm -rf wlp/

EXPOSE 12000