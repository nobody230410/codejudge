package com.yanzy.codejudge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanzy.codejudge.common.exception.BusinessException;
import com.yanzy.codejudge.common.exception.ErrorCodeEnum;
import com.yanzy.codejudge.mapper.UserMapper;
import com.yanzy.codejudge.pojo.User;
import com.yanzy.codejudge.service.UserService;
import com.yanzy.codejudge.vo.UserProfileVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void resetPasswordByAdmin(Integer userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodeEnum.USER_NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public UserProfileVO getProfile(int userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodeEnum.USER_NOT_FOUND);
        }
        return new UserProfileVO(user.getId(), user.getUsername(), user.getEmail());
    }

    @Override
    public void updateEmail(int userId, String email) {
        String normalized = email == null ? "" : email.trim();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodeEnum.USER_NOT_FOUND);
        }
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getEmail, normalized).ne(User::getId, userId);
        if (userMapper.selectCount(qw) > 0) {
            throw new BusinessException(ErrorCodeEnum.EMAIL_ALREADY_IN_USE);
        }
        user.setEmail(normalized);
        userMapper.updateById(user);
    }

    @Override
    public void changePassword(int userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodeEnum.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCodeEnum.OLD_PASSWORD_INCORRECT);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
