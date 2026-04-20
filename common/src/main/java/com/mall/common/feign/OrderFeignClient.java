package com.mall.common.feign;

import com.mall.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单服务Feign接口
 */
@FeignClient(name = "order-service")
public interface OrderFeignClient {

    @PostMapping("/order/paySuccess")
    R<Void> paySuccess(@RequestParam("orderNo") String orderNo);
}
