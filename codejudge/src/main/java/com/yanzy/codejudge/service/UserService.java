package com.yanzy.codejudge.service;

import com.yanzy.codejudge.vo.UserProfileVO;

public interface UserService {

    void resetPasswordByAdmin(Integer userId, String newPassword);

    UserProfileVO getProfile(int userId);

    void updateEmail(int userId, String email);

    void changePassword(int userId, String oldPassword, String newPassword);
}
