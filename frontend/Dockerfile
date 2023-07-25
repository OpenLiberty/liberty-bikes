FROM icr.io/appcafe/open-liberty:kernel-slim-java11-openj9-ubi
ADD --chown=1001:0 build/libs/frontend.war /config/apps
COPY --chown=1001:0 src/main/liberty/config /config/
# Install necessary features
RUN features.sh

RUN printf 'httpPort=12000\n\
httpsPort=12005\n\
application.name=frontend.war' > /config/bootstrap.properties

EXPOSE 12000 12005