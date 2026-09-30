package com.example.spring_security_jwt.security.jwt;

import java.io.IOException;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.spring_security_jwt.security.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
public class AuthTokenFilter extends OncePerRequestFilter {

    //inyectamos las dependencias
    @SuppressWarnings("unused")
    private final JwtUtils jwtUtils;
    @SuppressWarnings("unused")
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            @SuppressWarnings("unused")
            String jwt = parseJwt(request); //en parseJwt() crear el metodo en quick fix
        } catch (Exception e) {

        }
        
    }

    private String parseJwt(HttpServletRequest request) {
        
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer: ")) {
            return authHeader.substring(7);
        }
        return null;    
    }

}
