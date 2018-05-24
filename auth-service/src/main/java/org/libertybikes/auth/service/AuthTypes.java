package org.libertybikes.auth.service;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
