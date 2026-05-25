package com.yanzy.codejudge.controller;

import com.yanzy.codejudge.auth.model.LoginUser;
import com.yanzy.codejudge.common.exception.BusinessException;
import com.yanzy.codejudge.common.exception.ErrorCodeEnum;
import com.yanzy.codejudge.dto.req.UserChangePasswordRequest;
import com.yanzy.codejudge.dto.req.UserUpdateEmailRequest;
import com.yanzy.codejudge.service.UserService;
import com.yanzy.codejudge.vo.UserProfileVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserProfileVO me() {
        int userId = currentUserId();
        return userService.getProfile(userId);
    }

    @PutMapping("/email")
    public UserProfileVO updateEmail(@RequestBody @Valid UserUpdateEmailRequest request) {
        int userId = currentUserId();
        userService.updateEmail(userId, request.getEmail());
        return userService.getProfile(userId);
    }

    @PutMapping("/password")
    public void changePassword(@RequestBody @Valid UserChangePasswordRequest request) {
        int userId = currentUserId();
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
    }

    private static int currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(ErrorCodeEnum.LOGIN_FAILED);
        }
        return loginUser.getId();
    }
}
