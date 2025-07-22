package com.nextphase.backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nextphase.backend.model.LocalUser;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JWTService {
    @Value("${jwt.algorithm.key}")
    private String algorithmKey;
    @Value("${jwt.issuer}")
    private String issuer;
    @Value("${jwt.expiryInSeconds}")
    private int expiryInSeconds;
    private Algorithm algorithm;
    private static final String USERNAME_KEY = "USERNAME";
    private static final String VERIFICATION_EMAIL_KEY = "VERIFICATION_EMAIL";
    private static final String RESET_PASSWORD_EMAIL_KEY = "RESET_PASSWORD_EMAIL";

    @PostConstruct
    public void postConstruct() {
        algorithm = Algorithm.HMAC256(algorithmKey);
    }

    public String generateJWT(LocalUser localUser) {
        return JWT.create()
                .withClaim(USERNAME_KEY, localUser.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * expiryInSeconds)))
                .withIssuer(issuer)
                .sign(algorithm);
    }
    public String generateVerificationJWT(LocalUser localUser) {
        return JWT.create()
                .withClaim(VERIFICATION_EMAIL_KEY, localUser.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * expiryInSeconds)))
                .withIssuer(issuer)
                .sign(algorithm);
    }
    public String generatePasswordResetJWT(LocalUser localUser) {
        return JWT.create()
                .withClaim(RESET_PASSWORD_EMAIL_KEY, localUser.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * 60 * 30)))
                .withIssuer(issuer)
                .sign(algorithm);
    }
    public String getResetPasswordEmail(String token){
        DecodedJWT jwt = JWT.require(algorithm).withIssuer(issuer).build().verify(token);
        return jwt.getClaim(RESET_PASSWORD_EMAIL_KEY).asString();
    }
    public String getUsername(String token) {
        DecodedJWT jwt = JWT.require(algorithm).withIssuer(issuer).build().verify(token);
        return jwt.getClaim(USERNAME_KEY).asString();
    }
}
