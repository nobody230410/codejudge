package com.yanzy.codejudge.auth.model.adapter;

import com.yanzy.codejudge.pojo.Admin;
import com.yanzy.codejudge.auth.model.LoginUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

public class AdminAdapter implements LoginUser {
    private final Admin admin;

    public AdminAdapter(Admin admin) {this.admin = admin;}

    @Override
    public int getId() {
        return admin.getId();
    }

    @Override
    public String getUsername() {
        return admin.getUsername();
    }

    @Override
    public String getUserType() {
        return "ADMIN";
    }

    @Override
    public String getPassword() {
        return admin.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_ADMIN");
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
