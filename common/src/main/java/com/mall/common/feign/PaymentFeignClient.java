package com.mall.common.feign;

import com.mall.common.dto.RefundDTO;
import com.mall.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 支付服务Feign接口
 */
@FeignClient(name = "payment-service")
public interface PaymentFeignClient {

    @PostMapping("/payment/refund")
    R<Void> refund(@RequestBody RefundDTO dto);
}
