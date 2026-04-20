package com.mall.common.feign;

import com.mall.common.dto.UserDTO;
import com.mall.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务Feign接口
 */
@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/user/{id}")
    R<UserDTO> getUserById(@PathVariable("id") Long id);
}
