package com.example.spring_security_jwt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@NoArgsConstructor 
@AllArgsConstructor 
@Data 
@Builder 
public class Role  {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;

    /*Esta anotación no es necesaria en este momento porque no necesitamos
    que Role sepa de la relación
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "roles")
    @JsonIgnore
    private Set<User> users = new HashSet<>();  */
}
