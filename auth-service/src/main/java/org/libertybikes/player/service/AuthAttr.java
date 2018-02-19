package org.libertybikes.player.service;

import java.security.Principal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;

//import com.ibm.websphere.security.jwt.JwtBuilder;
//import com.ibm.websphere.security.jwt.JwtToken;

//import com.ibm.websphere.security.WSSecurityException;
//import com.ibm.websphere.security.auth.CredentialDestroyedException;
//import com.ibm.websphere.security.auth.WSSubject;
//import com.ibm.websphere.security.cred.WSCredential;
//import com.ibm.websphere.security.jwt.JwtBuilder;
//import com.ibm.websphere.security.jwt.JwtToken;

@Path("/")
@ApplicationScoped
public class AuthAttr {

    private Object exptime;

    @Inject
    private Principal principal;

    @Context
    HttpServletRequest request;

    @Inject
    private JsonWebToken callerPrincipal;

    @GET
    @Path("/ryan")
    @Produces(MediaType.APPLICATION_JSON)
    public String getToken(/** @Context SecurityContext sec */
    ) {
        Cookie[] cookie = request.getCookies();
        String jwt = (String) request.getSession().getAttribute("jwt");
        // System.out.println("cookie: " + cookie.length);
        System.out.println("~header " + request.getHeader("Authorization"));

        return callerPrincipal.getName();
        //return jwt;

        //UserProfileManager.getUserProfile().getAccessToken()
//        String claimString = "";
//        Set<String> claims = callerPrincipal.getClaimNames();
//        for (String claim : claims) {
//            claimString += claim + ": " + callerPrincipal.getClaim(claim) + "\n";
//        }
//        return claimString;
//        return principal.getName();

//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow(new NetHttpTransport(), new JacksonFactory(), "key", "secret", Arrays
//                        .asList("https://www.googleapis.com/auth/userinfo.profile", "https://www.googleapis.com/auth/userinfo.email"));
//
//        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

        // Put the user info into a JWT Token
        // create() uses default settings.
        // For other settings, specify a JWTBuilder element in server.xml
        // and call create(builder id)
//        String user = principal.getName();
//        long exptime = 0;
//        String jwtTokenString = null;
//        try {
//            JwtBuilder builder = com.ibm.websphere.security.jwt.JwtBuilder.create("jwtUserBuilder");
//            builder.subject(user);
//            builder.claim("upn", user);
////        builder.claim("groups", groups);
//            builder.claim("iss", "https://localhost:8482/auth-service/token");//request.getRequestURL().toString());
//
//            JwtToken theToken = builder.buildJwt();
//            exptime = theToken.getClaims().getExpiration();
//            jwtTokenString = theToken.compact();
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//        // populate the bean for easy conversion to json
//        Token tb = new Token();
//        tb.setToken(jwtTokenString);
//        tb.setExpires(Long.toString(exptime));
//        return Response.ok(tb).build();
        //   return user;

//        URI uri = null;
//        try {
//            uri = new URI("public");
//        } catch (URISyntaxException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return Response.temporaryRedirect(uri).build();
    }

    @GET
    @Path("/public")
    public String publicResource() {
        return "This is a public resource";
    }

}
