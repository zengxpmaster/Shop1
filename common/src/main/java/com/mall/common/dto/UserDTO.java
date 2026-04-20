package com.mall.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息DTO
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private Integer role;
}
