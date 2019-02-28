FROM open-liberty:microProfile1
ADD build/libs/player-service.war /config/dropins
COPY src/main/liberty/config /config/
RUN printf 'httpPort=8081\n\
httpsPort=8444' > /config/bootstrap.properties

# Install 'curl' if we want to enable HEALTHCHECK
#  && apt-get update \
#  && apt-get install -y --no-install-recommends curl

# Not used for now because ideally we want to poll more quickly
# during the start-period but slower after container start
# HEALTHCHECK --interval=8s --timeout=3s --start-period=30s \
#  CMD curl -f http://localhost:8081/health || exit 1

EXPOSE 8081 8444