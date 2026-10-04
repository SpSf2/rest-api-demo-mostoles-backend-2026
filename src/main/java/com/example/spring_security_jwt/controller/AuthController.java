package com.example.spring_security_jwt.controller;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_security_jwt.model.ERole;
import com.example.spring_security_jwt.model.Role;
import com.example.spring_security_jwt.model.User;
import com.example.spring_security_jwt.payload.request.LoginRequest;
import com.example.spring_security_jwt.payload.request.SignupRequest;
import com.example.spring_security_jwt.payload.response.JwtResponse;
import com.example.spring_security_jwt.payload.response.MessageResponse;
import com.example.spring_security_jwt.repository.RoleRepository;
import com.example.spring_security_jwt.repository.UserRepository;
import com.example.spring_security_jwt.security.jwt.JwtUtils;
import com.example.spring_security_jwt.security.service.UserDetailsImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    // inyectamos dependencias
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    // METODO PARA REGISTRAR UN NUEVO USUARIO
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest,
            BindingResult validationResults) {

        LOGGER.info(">>> ENTRANDO EN /signup PARA: {}", signupRequest.getUsername());

        // 1. Validaciones del DTO
        if (validationResults.hasErrors()) {
            String errorMessage = validationResults.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            LOGGER.warn(">>> Error de validación: {}", errorMessage);
            return ResponseEntity.badRequest().body(new MessageResponse("Error de validación: " + errorMessage));
        }

        // 2. Validar duplicados
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            LOGGER.warn(">>> El usuario ya existe");
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El usuario ya existe"));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            LOGGER.warn(">>> El email ya existe");
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El email ya existe"));
        }

        // 3. Creación del objeto User
        User user = User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .build();

        Set<String> strRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: No se ha podido encontrar el rol"));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: No se ha podido encontrar el rol de admin"));
                        roles.add(adminRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: No se ha podido encontrar el rol de usuario"));
                        roles.add(userRole);
                        break;
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        LOGGER.info(">>> USUARIO GUARDADO CON ÉXITO");

        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    // Metodo para logearse un usuario que se ha registrado previamente
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, BindingResult result) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        try{
                SecurityContextHolder.getContext().setAuthentication(authentication);
       
                String jwt = jwtUtils.generateJwtToken(authentication);
      
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
      
                Set<String> roles = userDetails.getAuthorities()
                                                .stream()
                                                .map(item -> item.getAuthority())
                                               .collect(Collectors.toSet());

        // Mostrar por la consola los roles
        LOGGER.info("Roles del usuario: {}", roles);
        return ResponseEntity.ok(
                new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
   
            } catch (org.springframework.security.authentication.BadCredentialsException e) {
                  LOGGER.error("Credenciales incorrectas para el usuario: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Usuario o contraseña incorrectos");
          
            } catch (Exception e) {
                    LOGGER.error("Error durante la autenticación: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor: " + e.getMessage());
        }
    }

}
