package com.example.spring_security_jwt.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.spring_security_jwt.security.jwt.AuthEntryPointJwt;
import com.example.spring_security_jwt.security.jwt.AuthTokenFilter;
import com.example.spring_security_jwt.security.jwt.JwtUtils;
import com.example.spring_security_jwt.security.service.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration 
@EnableMethodSecurity /* la Anotación permite securedEnable = true, también hace jsr250Enable = true
 y la más importante es prePostEnable = true

Que se resume a poder asegurar o secürizar directamente los metodos de los controladores, es decir,
 donde se delegan las peticiones, concretamente los Endpoints   */

@RequiredArgsConstructor 
public class WebSecurityConfig {

    //inyectamos dependencias
    private final UserDetailsServiceImpl userDetailsService; //se usa la implementacion que ha sido creada
                                                            //en nuestro proyecto ya que el Service no es
                                                            //una clase creada en com.example
    @SuppressWarnings("unused")
    private final AuthEntryPointJwt unauthorizeHandle;
    private final JwtUtils jwtUtils;

    // 1º Bean
    @Bean 
    AuthTokenFilter authenticationJwtTokenFilter() {

        return new AuthTokenFilter(jwtUtils, userDetailsService);
    }

    // 2º Bean
    @Bean
    DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
            
    }

    // 3º Bean
    //se quita el private, porque no es necesario que sea privado aunque solo se utilice aqui
    @Bean
    public PasswordEncoder passwordEncoder() {
       
        return new BCryptPasswordEncoder();
    }

    // 4º Bean
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {

        return authConfig.getAuthenticationManager();
    }

    /*El Bean siguiente es el que hay que saber personalizar para adaptarlo a nuestro
    proyecto, todo lo demás es boilerplate (código repeptitivo) */

    // 5º Bean
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) {

        http.csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizeHandle))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**").permitAll()
                    .anyRequest().authenticated());

            http.authenticationProvider(authenticationProvider());

            http.addFilterBefore(authenticationJwtTokenFilter()
                             , UsernamePasswordAuthenticationFilter.class);

            return http.build();
    }
}