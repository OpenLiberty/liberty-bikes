package org.libertybikes.auth.service;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.libertybikes.auth.service.github.GitHubAuth;
import org.libertybikes.auth.service.google.GoogleAuth;

@Path("/")
@ApplicationScoped
public class AuthTypes {

    @Inject
    ConfigBean config;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> endpoints() {
        Set<String> types = new HashSet<>();

        if (config.checkGitHubConfig())
            types.add(GitHubAuth.class.getSimpleName());
        if (config.checkGoogleConfig())
            types.add(GoogleAuth.class.getSimpleName());

        return types;
    }

}
