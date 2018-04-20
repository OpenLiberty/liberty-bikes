package org.libertybikes.auth.service;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
public class AuthApp extends Application {

    public static final String HTTPS_AUTH_SERVICE = "https://localhost:8482/auth-service";
    public static final String FRONTEND_URL = "http://localhost:12000/login";
}
