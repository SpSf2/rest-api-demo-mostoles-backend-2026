package com.example.spring_security_jwt.security.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component 
public class JwtUtils {

    private static final Logger LOOGER = LoggerFactory.getLogger(JwtUtils.class);

    @Value ("${demo.app.jwtSecret}")// viene del archivo application.properties
    private String jwtSecret;

    @Value ("${demo.app.jwtExpirationMs}")// viene del archivo application.properties
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {

        return null;

    }
}