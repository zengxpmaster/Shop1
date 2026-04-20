package com.mall.payment.service.impl;

import com.mall.common.dto.RefundDTO;
import com.mall.common.entity.Account;
import com.mall.common.entity.PaymentRecord;
import com.mall.common.entity.RefundRecord;
import com.mall.common.feign.OrderFeignClient;
import com.mall.payment.mapper.AccountMapper;
import com.mall.payment.mapper.PaymentRecordMapper;
import com.mall.payment.mapper.RefundRecordMapper;
import com.mall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    @Autowired
    private RefundRecordMapper refundRecordMapper;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Override
    @Transactional
    public void pay(String orderNo, Long userId, BigDecimal amount) {
        // 扣减账户余额
        Account account = accountMapper.selectById(userId);
        if (account == null) {
            throw new RuntimeException("账户不存在");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("余额不足");
        }
        account.setBalance(account.getBalance().subtract(amount));
        account.setUpdateTime(LocalDateTime.now());
        accountMapper.updateById(account);

        // 创建支付记录
        PaymentRecord record = new PaymentRecord();
        record.setPaymentNo(UUID.randomUUID().toString().replace("-", ""));
        record.setOrderNo(orderNo);
        record.setUserId(userId);
        record.setAmount(amount);
        record.setStatus(1); // 支付成功
        record.setPayMethod("BALANCE");
        record.setCreateTime(LocalDateTime.now());
        record.setPayTime(LocalDateTime.now());
        paymentRecordMapper.insert(record);

        // 通知订单服务支付成功
        orderFeignClient.paySuccess(orderNo);
    }

    @Override
    @Transactional
    public void refund(RefundDTO dto) {
        // 退还账户余额
        Account account = accountMapper.selectById(dto.getUserId());
        if (account == null) {
            throw new RuntimeException("账户不存在");
        }
        account.setBalance(account.getBalance().add(dto.getAmount()));
        account.setUpdateTime(LocalDateTime.now());
        accountMapper.updateById(account);

        // 创建退款记录
        RefundRecord refundRecord = new RefundRecord();
        refundRecord.setPaymentId(dto.getPaymentId());
        refundRecord.setOrderNo(dto.getOrderNo());
        refundRecord.setUserId(dto.getUserId());
        refundRecord.setAmount(dto.getAmount());
        refundRecord.setStatus(2); // 退款成功
        refundRecord.setReason(dto.getReason());
        refundRecord.setCreateTime(LocalDateTime.now());
        refundRecord.setRefundTime(LocalDateTime.now());
        refundRecordMapper.insert(refundRecord);
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        Account account = accountMapper.selectById(userId);
        return account != null ? account.getBalance() : BigDecimal.ZERO;
    }
}
