package com.example.demo.overtimeEntry.event;

import com.example.demo.notification.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OvertimeSettlementListener {

    private final SmsService smsService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OvertimeSettledEvent event) {

        try {

            smsService.sendSettlementSms(event.getWorkerId(), event.getMonth(), event.getAmount());

        } catch (Exception ex) {

            log.error("SMS sending failed for worker {}", event.getWorkerId(), ex);
        }
    }
}