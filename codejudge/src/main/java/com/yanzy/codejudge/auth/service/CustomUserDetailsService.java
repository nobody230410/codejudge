package com.yanzy.codejudge.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanzy.codejudge.auth.LoginContext;
import com.yanzy.codejudge.auth.model.adapter.AdminAdapter;
import com.yanzy.codejudge.auth.model.adapter.UserAdapter;
import com.yanzy.codejudge.mapper.AdminMapper;
import com.yanzy.codejudge.mapper.UserMapper;
import com.yanzy.codejudge.pojo.Admin;
import com.yanzy.codejudge.pojo.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;
    private final AdminMapper adminMapper;

    public CustomUserDetailsService(UserMapper userMapper, AdminMapper adminMapper) {
        this.userMapper = userMapper;
        this.adminMapper = adminMapper;
    }

    /**
     * 核心方法：Spring Security 会自动调用这个方法
     * @param username 用户输入的账号
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String userType = LoginContext.getUserType();

        if ("ADMIN".equals(userType)) {
            LambdaQueryWrapper<Admin> adminQw = new LambdaQueryWrapper<>();
            adminQw.eq(Admin::getUsername, username);
            Admin admin = adminMapper.selectOne(adminQw);
            if (admin == null) throw new UsernameNotFoundException("管理员不存在");
            return new AdminAdapter(admin);
        }
        if ("USER".equals(userType)) {
            LambdaQueryWrapper<User> userQw = new LambdaQueryWrapper<>();
            userQw.eq(User::getUsername, username);
            User user = userMapper.selectOne(userQw);
            if (user == null) throw new UsernameNotFoundException("用户不存在");
            return new UserAdapter(user);
        }

        // JWT 校验等场景：ThreadLocal 未设置，按用户名依次匹配普通用户与管理员
        LambdaQueryWrapper<User> userQw = new LambdaQueryWrapper<>();
        userQw.eq(User::getUsername, username);
        User user = userMapper.selectOne(userQw);
        if (user != null) {
            return new UserAdapter(user);
        }
        LambdaQueryWrapper<Admin> adminQw = new LambdaQueryWrapper<>();
        adminQw.eq(Admin::getUsername, username);
        Admin admin = adminMapper.selectOne(adminQw);
        if (admin != null) {
            return new AdminAdapter(admin);
        }
        throw new UsernameNotFoundException("用户不存在");
    }
}
