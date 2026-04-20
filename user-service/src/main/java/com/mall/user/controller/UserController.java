package com.mall.user.controller;

import com.mall.common.dto.UserDTO;
import com.mall.common.result.R;
import com.mall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public R<UserDTO> getUserById(@PathVariable Long id) {
        return R.ok(userService.getUserById(id));
    }

    @PostMapping("/register")
    public R<Void> register(@RequestBody UserDTO userDTO) {
        userService.register(userDTO);
        return R.ok();
    }

    @PostMapping("/login")
    public R<String> login(@RequestParam String username, @RequestParam String password) {
        String token = userService.login(username, password);
        return R.ok(token);
    }
}
