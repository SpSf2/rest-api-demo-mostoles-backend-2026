package com.example.spring_security_jwt.security.jwt;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.spring_security_jwt.security.service.UserDetailsImpl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component 
public class JwtUtils {

    private static final Logger LOOGER = LoggerFactory.getLogger(JwtUtils.class);

    @Value ("${demo.app.jwtSecret}")// viene del archivo application.properties
    private String jwtSecret;

    @Value ("${demo.app.jwtExpirationMs}")// viene del archivo application.properties
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
            .subject(userPrincipal.getUsername())
            .issuedAt(new Date())
            .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
            .signWith(key())
            .compact();
    }

    private Key key() {

        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {

        return Jwts.parser().verifyWith((SecretKey) key()).build()    // parseo el key
                   .parseSignedClaims(token).getPayload().getSubject();        
    }

    // Esta función se encarga de verificar si el token es válido o no
    public boolean validateJwtToken(String authtoken) {

        try {

            Jwts.parser().verifyWith((SecretKey) key()).build().parse(authtoken);
            return true;
        } catch (MalformedJwtException e) {
            LOOGER.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOOGER.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOOGER.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOOGER.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}