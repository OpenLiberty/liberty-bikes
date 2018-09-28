FROM websphere-liberty:beta
ADD build/libs/auth-service.zip /
RUN unzip auth-service.zip
RUN echo "frontend_url=http://lb-frontend:12000/login\
auth_url=https://lb-auth:8082/auth-service\
" > wlp/usr/servers/auth-service/server.env
RUN mv wlp/usr/servers/auth-service/* /config/
RUN rm -rf wlp/

EXPOSE 8082