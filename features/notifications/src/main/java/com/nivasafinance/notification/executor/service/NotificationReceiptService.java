package com.nivasafinance.notification.executor.service;

import com.nivasafinance.notification.executor.entity.NotificationReceipt;
import com.nivasafinance.notification.executor.repository.NotificationReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationReceiptService {

    private final NotificationReceiptRepository notificationReceiptRepository;

    @Transactional
    public NotificationReceipt save(NotificationReceipt receipt) {
        log.info("Persisting notification receipt {}", receipt.getId());
        return notificationReceiptRepository.save(receipt);
    }
}


