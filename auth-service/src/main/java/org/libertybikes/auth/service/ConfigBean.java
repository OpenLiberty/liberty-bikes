package org.libertybikes.auth.service;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public class ConfigBean {

    // REQUIRED GOOGLE SETTINGS
    public static final String GOOGLE_KEY = "googleOAuthConsumerKey";
    public static final String GOOGLE_SECRET = "googleOAuthConsumerSecret";

    // REQUIRED GITHUB SETTINGS
    public static final String GITHUB_KEY = "github_key";
    public static final String GITHUB_SECRET = "github_secret";

    @Inject
    @ConfigProperty(name = "auth_url", defaultValue = "https://localhost:8482/auth-service")
    public String authUrl;

    @Inject
    @ConfigProperty(name = "frontend_url", defaultValue = "http://localhost:12000/login")
    public String frontendUrl;

    // Give the required config properties default values so that people don't need to set up
    // all auth types in order to use any of them (app deployment fails if any config can't be injected otherwise)
    @Inject
    @ConfigProperty(name = GITHUB_KEY, defaultValue = "")
    public String github_key;

    @Inject
    @ConfigProperty(name = GITHUB_SECRET, defaultValue = "")
    public String github_secret;

    @Inject
    @ConfigProperty(name = GOOGLE_KEY, defaultValue = "")
    public String google_key;

    @Inject
    @ConfigProperty(name = GOOGLE_SECRET, defaultValue = "")
    public String google_secret;

    @PostConstruct
    public void validate() {
        checkGitHubConfig();
        checkGoogleConfig();
    }

    public boolean checkGitHubConfig() {
        boolean success = true;
        if (github_key.isEmpty()) {
            success = false;
            System.out.println("WARN: " + GITHUB_KEY + " not set, GitHub auth will not be available...");
        }
        if (github_secret.isEmpty()) {
            success = false;
            System.out.println("WARN: " + GITHUB_SECRET + " not set, GitHub auth will not be available...");
        }
        return success;
    }

    public boolean checkGoogleConfig() {
        boolean success = true;
        if (google_key.isEmpty()) {
            success = false;
            System.out.println("WARN: " + GOOGLE_KEY + " not set, GitHub auth will not be available...");
        }
        if (google_secret.isEmpty()) {
            success = false;
            System.out.println("WARN: " + GOOGLE_SECRET + " not set, GitHub auth will not be available...");
        }
        return success;
    }

}
