FROM open-liberty:microProfile1
ADD build/libs/auth-service.war /config/dropins
COPY src/main/liberty/config /config/
RUN printf 'frontend_url=http://lb-frontend:12000/login\n\
auth_url=https://lb-auth:8082/auth-service' > /config/server.env
RUN printf 'httpPort=8082\n\
httpsPort=8482' > /config/bootstrap.properties

EXPOSE 8082 8482