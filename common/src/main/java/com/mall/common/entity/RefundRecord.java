package com.mall.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录实体
 */
@Data
@TableName("refund_record")
public class RefundRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long paymentId;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private Integer status; // 0-待审核 1-审核通过 2-退款成功 3-审核拒绝
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime refundTime;
}
