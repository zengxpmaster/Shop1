package com.mall.common.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户实体
 */
@Data
@TableName("account")
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long userId;
    private BigDecimal balance;
    private LocalDateTime updateTime;
}
