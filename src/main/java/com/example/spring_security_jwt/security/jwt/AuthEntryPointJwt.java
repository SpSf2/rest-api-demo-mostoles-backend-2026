package com.example.spring_security_jwt.security.jwt;
// Clase para poder manejar las excepciones de tipo autenticación en nuestra app,
// es decir, las excepciones que se generen en el momento de la autenticación.

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component 
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        
                LOGGER.error("Unauthoraized error: {}", authException.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Fallo de autenticación");
    }

}
