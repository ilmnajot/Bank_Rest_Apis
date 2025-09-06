package com.example.bankcards.entity;

import com.example.bankcards.entity.base.BaseEntity;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {

    @Column(nullable = false)
    private String fullName;
    @Column(unique = true)
    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)

    private Role role;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private UserStatus userStatus;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
