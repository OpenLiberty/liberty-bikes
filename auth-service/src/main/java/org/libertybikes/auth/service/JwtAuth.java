
package org.libertybikes.auth.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * A base class for bikes auth implementations that return signed JWTs to the client.
 */
public abstract class JwtAuth {

    @Resource(lookup = "jwtKeyStore")
    protected String keyStore;

    @Inject
    @ConfigProperty(name = "jwtKeyStorePassword", defaultValue = "secret")
    String keyStorePW;

    @Inject
    @ConfigProperty(name = "jwtKeyStoreAlias", defaultValue = "bike")
    String keyStoreAlias;

    protected static Key signingKey = null;

    /**
     * Obtain the key we'll use to sign the jwts we issue.
     */
    private synchronized void getKeyStoreInfo() throws IOException {
        if (signingKey != null)
            return;
        try {
            // load up the keystore
            FileInputStream is = new FileInputStream(keyStore);
            KeyStore signingKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
            signingKeystore.load(is, keyStorePW.toCharArray());
            signingKey = signingKeystore.getKey(keyStoreAlias, keyStorePW.toCharArray());
        } catch (Exception e) {
            throw new IOException(e);
        }

    }

    /**
     * Obtain a JWT with the claims supplied. The key "id" will be used to set
     * the JWT subject.
     *
     * @param claims map of string->string for claim data to embed in the jwt.
     * @return jwt encoded as string, ready to send to http.
     */
    protected String createJwt(Map<String, String> claims) throws IOException {
        if (signingKey == null) {
            getKeyStoreInfo();
        }

        Claims onwardsClaims = Jwts.claims();

        // Add all the remaining claims as-is.
        onwardsClaims.putAll(claims);

        // Set the subject using the "id" field from our claims map.
        onwardsClaims.setSubject(claims.get("id"));

        onwardsClaims.setId(claims.get("id"));

        // We'll use this claim to know this is a user token
        onwardsClaims.setAudience("client");

        onwardsClaims.setIssuer("https://libertybikes.mybluemix.net");
        // we set creation time to 24hrs ago, to avoid timezone issues in the
        // browser verification of the jwt.
        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.HOUR, -24);
        onwardsClaims.setIssuedAt(calendar1.getTime());

        // client JWT has 24 hrs validity from now.
        Calendar calendar2 = Calendar.getInstance();
        calendar2.add(Calendar.HOUR, 24);
        onwardsClaims.setExpiration(calendar2.getTime());

        // finally build the new jwt, using the claims we just built, signing it
        // with our signing key, and adding a key hint as kid to the encryption header,
        // which is optional, but can be used by the receivers of the jwt to know which
        // key they should verify it with.
        return Jwts.builder()
                        .setHeaderParam("kid", "bike")
                        .setHeaderParam("alg", "RS256")
                        .setClaims(onwardsClaims)
                        .signWith(SignatureAlgorithm.RS256, signingKey)
                        .compact();
    }

}