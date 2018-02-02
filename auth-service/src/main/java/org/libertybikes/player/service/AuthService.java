package org.libertybikes.player.service;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
@ApplicationScoped
public class AuthService {

	@GET
	@Path("/token")
	public String getToken() {
		// TODO this should be using MP JWT to hand back a token
		return "Super secret token";
	}

}
