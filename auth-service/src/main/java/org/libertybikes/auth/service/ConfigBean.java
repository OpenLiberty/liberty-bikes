package org.libertybikes.auth.service;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.libertybikes.auth.service.github.GitHubAuth;
import org.libertybikes.auth.service.google.GoogleAuth;
import org.libertybikes.auth.service.twitter.TwitterAuth;

@Singleton
@BeanDefiner
public class ConfigBean {

    // REQUIRED GOOGLE SETTINGS
    public static final String GOOGLE_KEY = "google_key";
    public static final String GOOGLE_SECRET = "google_secret";

    // REQUIRED GITHUB SETTINGS
    public static final String GITHUB_KEY = "github_key";
    public static final String GITHUB_SECRET = "github_secret";

    // REQUIRED TWITTER SETTINGS
    public static final String TWITTER_KEY = "twitter_key";
    public static final String TWITTER_SECRET = "twitter_secret";

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

    @Inject
    @ConfigProperty(name = TWITTER_KEY, defaultValue = "")
    public String twitter_key;

    @Inject
    @ConfigProperty(name = TWITTER_SECRET, defaultValue = "")
    public String twitter_secret;

    @PostConstruct
    public void validate() {
        checkGitHubConfig();
        checkGoogleConfig();
    }

    public Set<String> getConfiguredTypes() {
        Set<String> types = new HashSet<>();
        if (checkGitHubConfig())
            types.add(GitHubAuth.class.getSimpleName());
        if (checkGoogleConfig())
            types.add(GoogleAuth.class.getSimpleName());
        if (checkTwitterConfig())
            types.add(TwitterAuth.class.getSimpleName());
        return types;
    }

    private boolean checkGitHubConfig() {
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

    private boolean checkGoogleConfig() {
        boolean success = true;
        if (google_key.isEmpty()) {
            success = false;
            System.out.println("WARN: " + GOOGLE_KEY + " not set, Google auth will not be available...");
        }
        if (google_secret.isEmpty()) {
            success = false;
            System.out.println("WARN: " + GOOGLE_SECRET + " not set, Google auth will not be available...");
        }
        return success;
    }

    private boolean checkTwitterConfig() {
        boolean success = true;
        if (twitter_key.isEmpty()) {
            success = false;
            System.out.println("WARN: " + TWITTER_KEY + " not set, Twitter auth will not be available...");
        }
        if (twitter_secret.isEmpty()) {
            success = false;
            System.out.println("WARN: " + TWITTER_SECRET + " not set, Twitter auth will not be available...");
        }
        return success;
    }

}
