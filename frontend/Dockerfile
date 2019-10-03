FROM open-liberty:19.0.0.9-microProfile3-java11
ADD --chown=1001:0 build/libs/frontend.war /config/apps
COPY --chown=1001:0 src/main/liberty/config /config/
RUN printf 'httpPort=12000\n\
httpsPort=12005\n\
application.name=frontend.war' > /config/bootstrap.properties

EXPOSE 12000 12005