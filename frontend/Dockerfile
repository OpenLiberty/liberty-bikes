FROM open-liberty:microProfile1
ADD build/libs/frontend.war /config/apps
COPY src/main/liberty/config /config/
RUN printf 'httpPort=12000\n\
httpsPort=12005\n\
application.name=frontend.war' > /config/bootstrap.properties

EXPOSE 12000 12005