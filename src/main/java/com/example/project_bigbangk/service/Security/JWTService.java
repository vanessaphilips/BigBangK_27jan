package com.example.project_bigbangk.service.Security;
/**
 * This class generates and valitades JWT
 * @author Pieter Jan Bleichrodt
 * Creation date 12/3/2021
 */

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JWTService implements ITokenService {

    private final Logger logger = LoggerFactory.getLogger(JWTService.class);
    private final long EXPIRATION_TIME = 3600000;  //milliseconds
    private final String ISSUER = "BigBangk";  //milliseconds
    private ISecretKeyService secretKeyService;
    private Algorithm ALGORITHM;
    private final String AUDIENCE = "https://www.bigbangk.com";
    private JWTVerifier verifier;

    @Autowired
    public JWTService(ISecretKeyService secretKeyService) {
        super();
        logger.info("New JWTService");
        this.secretKeyService = secretKeyService;
        ALGORITHM = Algorithm.HMAC256(this.secretKeyService.toString());
        verifier = JWT.require(ALGORITHM)
                .withIssuer(ISSUER)
                .build();
    }

    @Override
    public String getToken(String email, String firstName) {
        String token = null;
        try {
            long timeNow = System.currentTimeMillis();
            Date issuedAt = new Date(timeNow);
            Date experationDate = new Date(timeNow + EXPIRATION_TIME);
            token =JWT.create().withIssuer(ISSUER).withIssuedAt(issuedAt)
                    .withExpiresAt(experationDate)
                    .withAudience(AUDIENCE)
                    .withSubject(email)
                    .withClaim("firstName", firstName)
                    .withClaim("email", email)
                    .withClaim("role", Role.CLIENT.toString())
                    .sign(ALGORITHM);
        } catch (JWTCreationException exception) {
            logger.error(exception.getMessage());
        }
        return token;
    }

    @Override
    public boolean authenticateToken(String token) {

        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            if (decodedJWT.getExpiresAt().after(new Date(System.currentTimeMillis()))) {
                return true;
                //TODO refresh token
            }
        } catch (JWTVerificationException exception) {
            logger.error(exception.getMessage());
        }
        return false;
    }

    /**
     * @param token decode a JWT
     * @return email
     */
    @Override
    public String getEmailFromToken(String token) {
        try {
            return verifier.verify(token).getClaim("email").asString();
        } catch (JWTVerificationException exception) {
            logger.error(exception.getMessage());
        }
        return null;
    }
}