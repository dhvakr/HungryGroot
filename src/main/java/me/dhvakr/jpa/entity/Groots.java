package me.dhvakr.jpa.entity;

import me.dhvakr.jpa.FoodCountHistory;
import me.dhvakr.enums.Roles;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "groots")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Groots extends AbstractEntity {

    //~ initializers =======================================================================================================================

    // Email is username as default
    @Column(name = "username", unique = true, nullable = false)
    String username;

    @Column(name = "official_name", nullable = false)
    String name;

    @Column(name = "display_name")
    String displayName;

    @Column(name = "forget_password_key")
    @Length(min = 4, max = 10)
    String forgotPasswordKey;

    @JsonIgnore
    @Column(name = "password")
    @Length(min = 8, max = 64)
    String password;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    Set<Roles> roles;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "groots_food_history", joinColumns = @JoinColumn(name = "groot_id"))
    Set<FoodCountHistory> foodCountHistory = new HashSet<>();
}
