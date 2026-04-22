package com.nivasafinance.features.leadqueues.exception;

import org.springframework.context.MessageSource;
import com.nivasafinance.common.exception.ExceptionUtils;
import java.io.Serial;

public class QueueConfigOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public QueueConfigOperationException(String message) {
        super(message);
    }
    
    public static QueueConfigOperationException saveFailed(MessageSource messageSource) {
        return new QueueConfigOperationException(ExceptionUtils.createLocalizedMessage("queue.config.operation.save.failed", null, messageSource));
    }

    public static QueueConfigOperationException updateLastReorderTimeFailed(MessageSource messageSource) {
        return new QueueConfigOperationException(ExceptionUtils.createLocalizedMessage("queue.config.operation.update.last.reorder.time.failed", null, messageSource));
    }
}
