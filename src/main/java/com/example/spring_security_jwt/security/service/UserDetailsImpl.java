package com.example.spring_security_jwt.security.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.spring_security_jwt.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor 
@AllArgsConstructor 
@Data 
@Builder 
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private long id;
    
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetails build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName().name()))
            .collect(Collectors.toList());
        return new UserDetailsImpl( user.getId(),
                                    user.getUsername(),
                                    user.getEmail(),
                                    user.getPassword(),
                                    authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
       
        return password;
    }

    @Override
    public String getUsername() {
        
        return username;
    }

}
