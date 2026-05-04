package com.nivasafinance.features.leadqueues.service;

import com.nivasafinance.features.leadqueues.entity.QueueConfig;

public interface QueueReorderService {
    void reorderQueue(String queueConfigName);
    void reorderQueue(QueueConfig config);
}
