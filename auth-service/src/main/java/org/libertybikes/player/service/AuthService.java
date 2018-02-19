package org.libertybikes.player.service;

import java.io.IOException;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.security.jwt.JwtBuilder;
import com.ibm.websphere.security.jwt.JwtToken;

//import com.ibm.websphere.security.jwt.JwtBuilder;
//import com.ibm.websphere.security.jwt.JwtToken;

//import com.ibm.websphere.security.WSSecurityException;
//import com.ibm.websphere.security.auth.CredentialDestroyedException;
//import com.ibm.websphere.security.auth.WSSubject;
//import com.ibm.websphere.security.cred.WSCredential;
//import com.ibm.websphere.security.jwt.JwtBuilder;
//import com.ibm.websphere.security.jwt.JwtToken;

@WebServlet("/token")
public class AuthService extends HttpServlet {

    private static final long serialVersionUID = 2202953742584558018L;

//    @Inject
//    private JsonWebToken callerPrincipal;

    private static final Jsonb jsonb = JsonbBuilder.create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String user = req.getUserPrincipal().getName();
        long exptime = 0;
        String jwtTokenString = null;
        try {
            JwtBuilder builder = JwtBuilder.create("jwtUserBuilder");
            builder.subject(user);
            builder.claim("upn", user);
//        builder.claim("groups", groups);
            builder.claim("iss", "https://localhost:8482/auth-service/token");//request.getRequestURL().toString());

            JwtToken theToken = builder.buildJwt();
            exptime = theToken.getClaims().getExpiration();
            jwtTokenString = theToken.compact();
        } catch (Exception e) {
            System.out.println(e);
        }
        // populate the bean for easy conversion to json
        Token tb = new Token();
        tb.token = jwtTokenString;
        tb.expires = Long.toString(exptime);
        req.getSession().setAttribute("jwt", jwtTokenString);
        resp.getWriter().println(jsonb.toJson(tb));
        resp.setHeader("Authorization", "Bearer " + jwtTokenString);
        resp.sendRedirect("ryan");
    }

}
