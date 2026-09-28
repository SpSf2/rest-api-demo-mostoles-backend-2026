package com.example.spring_security_jwt.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.boot.internal.Abstract;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "users", uniqueConstraints = 
     @UniqueConstraint(columnNames = {"username", "email"}))
@NoArgsConstructor  
@AllArgsConstructor 
@Data 
@Builder 
public class User  {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private long id;

    @NotBlank 
    @Size (min = 3, max = 20)
    private String username;

    @NotBlank 
    @Size (max = 45)
    @Email
    private String email;

    @NotBlank 
    @Size (min = 6, max = 120)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
