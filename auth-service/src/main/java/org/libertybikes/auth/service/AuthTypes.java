package org.libertybikes.auth.service;

import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
@ApplicationScoped
public class AuthTypes {

    @Inject
    ConfigBean config;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> endpoints() {
        return config.getConfiguredTypes();
    }

}
