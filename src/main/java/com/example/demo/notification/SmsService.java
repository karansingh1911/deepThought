package com.example.demo.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class SmsService {

    public void sendSettlementSms(Long workerId, String month, BigDecimal amount) {

        log.info("SMS SENT -> Worker={} Month={} Amount={}", workerId, month, amount);
    }
}