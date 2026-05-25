package com.yanzy.codejudge.controller;

import com.yanzy.codejudge.dto.req.AdminResetUserPasswordRequest;
import com.yanzy.codejudge.mapper.UserMapper;
import com.yanzy.codejudge.pojo.User;
import com.yanzy.codejudge.service.UserService;
import com.yanzy.codejudge.vo.AdminUserItemVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserMapper userMapper;
    private final UserService userService;

    public AdminController(UserMapper userMapper, UserService userService) {
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<AdminUserItemVO> listUsers() {
        List<User> users = userMapper.selectList(null);
        return users.stream()
                .map(u -> new AdminUserItemVO(u.getId(), u.getUsername(), u.getEmail()))
                .toList();
    }

    @PutMapping("/users/{id}/password")
    public void resetUserPassword(
            @PathVariable("id") Integer id,
            @RequestBody @Valid AdminResetUserPasswordRequest request) {
        userService.resetPasswordByAdmin(id, request.getNewPassword());
    }
}
