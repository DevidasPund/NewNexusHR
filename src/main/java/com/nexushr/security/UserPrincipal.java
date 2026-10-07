package com.nexushr.security;

import com.nexushr.common.enums.Role;
import com.nexushr.employee.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Spring Security principal backed by an {@link Employee}. */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final boolean enabled;

    public UserPrincipal(Long id, String email, String passwordHash, Role role, boolean enabled) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
    }

    public static UserPrincipal from(Employee e) {
        return new UserPrincipal(
                e.getId(),
                e.getEmail(),
                e.getPasswordHash(),
                e.getRole(),
                e.getStatus() != com.nexushr.common.enums.EmployeeStatus.INACTIVE
        );
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
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
        return enabled;
    }
}
