package com.mall.user.service;

import com.mall.common.dto.UserDTO;

public interface UserService {

    UserDTO getUserById(Long id);

    void register(UserDTO userDTO);

    String login(String username, String password);
}
