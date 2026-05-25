package com.yanzy.codejudge.auth.model.adapter;

import com.yanzy.codejudge.auth.model.LoginUser;
import com.yanzy.codejudge.pojo.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

public class UserAdapter implements LoginUser {
    private final User user;

    public UserAdapter(User user) { this.user = user; }

    @Override
    public int getId() { return user.getId(); }

    @Override
    public String getUsername() { return user.getUsername(); }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUserType() {
        return "USER";
    }

    @Override
    public String getEmail() {
        return user.getEmail();
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");
    }



}
