package com.nivasafinance.features.leadqueues.exception;

import org.springframework.context.MessageSource;
import com.nivasafinance.common.exception.ExceptionUtils;
import java.io.Serial;

public class LeadQueueOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LeadQueueOperationException(String message) {
        super(message);
    }

    public LeadQueueOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static LeadQueueOperationException saveFailed(MessageSource messageSource) {
        return new LeadQueueOperationException(ExceptionUtils.createLocalizedMessage("queue.reorder.operation.failed", null, messageSource));
    }

    public static LeadQueueOperationException saveAllFailed(MessageSource messageSource) {
        return new LeadQueueOperationException(ExceptionUtils.createLocalizedMessage("queue.reorder.operation.save.all.failed", null, messageSource));
    }

    public static LeadQueueOperationException updateLastReorderTimeFailed(MessageSource messageSource) {
        return new LeadQueueOperationException(ExceptionUtils.createLocalizedMessage("queue.reorder.operation.update.last.reorder.time.failed", null, messageSource));
    }

    public static LeadQueueOperationException dataProviderExecutionFailed(
            String queueName, MessageSource messageSource, Throwable cause) {
        return new LeadQueueOperationException(
                ExceptionUtils.createLocalizedMessage(
                        "error.lead.queue.data.provider.execution.failed", new Object[]{queueName}, messageSource),
                cause);
    }
}