package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.entity.CallLogLead;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.repository.CallLogLeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadCallReadService;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.lead.dto.LeadCallLogResponse;
import com.nivasafinance.features.lead.dto.LeadCallSummaryResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LeadCallReadServiceImpl implements LeadCallReadService {

    private final CallReadService callReadService;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final CallLogLeadRepositoryWrapper callLogLeadRepositoryWrapper;

    @Override
    public LeadCallSummaryResponse getCallSummary(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        return LeadCallSummaryResponse.fromEntity(lead.getCallSummaryDetails());
    }

    @Override
    @Transactional
    public LeadCallSummaryResponse refreshCallSummary(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        recalculateLeadCallSummary(lead.getId());
        return getCallSummary(leadIdentifier);
    }

    @Override
    @Transactional
    public void recalculateLeadCallSummary(Long leadId) {
        Lead lead = leadRepositoryWrapper.findByIdWithException(leadId);
        List<CallLogLead> links = callLogLeadRepositoryWrapper.findAllByLeadIdOrderByCallLogIdDesc(leadId);

        if (links.isEmpty()) {
            lead.setCallSummaryDetails(emptyCallSummaryDetails());
            leadRepositoryWrapper.saveWithException(lead);
            return;
        }

        List<Long> ids = links.stream().map(CallLogLead::getCallLogId).collect(Collectors.toList());
        List<CallLogResponse> logs = callReadService.getCallLogsByIDs(ids);
        lead.setCallSummaryDetails(computeCallSummaryDetails(logs));
        leadRepositoryWrapper.saveWithException(lead);
    }

    @Override
    public PaginatedResponse<LeadCallLogResponse> getCallLogs(UUID leadIdentifier, PaginationRequest paginationRequest) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        int limit = paginationRequest.getLimit();
        int offset = paginationRequest.getOffset();
        int page = offset / limit;

        Page<CallLogLead> mappingPage = callLogLeadRepositoryWrapper.findByLeadId(
                lead.getId(), PageRequest.of(page, limit));

        if (mappingPage.isEmpty()) {
            return new PaginatedResponse<>(
                    List.of(),
                    PaginationInfo.builder()
                            .limit(limit).offset(offset)
                            .totalElements(0).totalPages(0).currentPage(0)
                            .hasNext(false).hasPrevious(false)
                            .build());
        }

        List<Long> pageIds = mappingPage.getContent().stream()
                .map(CallLogLead::getCallLogId)
                .collect(Collectors.toList());

        List<CallLogResponse> callLogResponses = callReadService.getCallLogsByIDs(pageIds);
        Map<Long, CallLogResponse> byId = callLogResponses.stream()
                .collect(Collectors.toMap(CallLogResponse::getId, Function.identity(), (a, b) -> a));

        List<LeadCallLogResponse> leadCallLogResponses = mappingPage.getContent().stream()
                .map(link -> {
                    CallLogResponse detail = byId.get(link.getCallLogId());
                    return LeadCallLogResponse.builder()
                            .callLogDetails(detail)
                            .contactId(link.getContactId())
                            .build();
                })
                .collect(Collectors.toList());

        long total = mappingPage.getTotalElements();
        int totalPages = mappingPage.getTotalPages();
        return new PaginatedResponse<>(
                leadCallLogResponses,
                PaginationInfo.builder()
                        .limit(limit)
                        .offset(offset)
                        .totalElements((int) total)
                        .totalPages(totalPages)
                        .currentPage(page)
                        .hasNext(mappingPage.hasNext())
                        .hasPrevious(mappingPage.hasPrevious())
                        .build()
        );
    }

    private static Lead.CallSummaryDetails emptyCallSummaryDetails() {
        return Lead.CallSummaryDetails.builder()
                .totalOutboundCalls(0)
                .outboundConnectedCalls(0)
                .bestTimeToCall(null)
                .lastConnectedCallAt(null)
                .lastCallAttemptAt(null)
                .averageOutboundTalkDurationSeconds(null)
                .consecutiveNoAnswers(0)
                .build();
    }

    private static Lead.CallSummaryDetails computeCallSummaryDetails(List<CallLogResponse> logs) {
        int outTotal = 0;
        int outConn = 0;
        long outDurSum = 0L;
        int outDurCount = 0;

        for (CallLogResponse log : logs) {
            if (log.getDirection() != CallDirection.OUTBOUND) {
                continue;
            }
            outTotal++;
            boolean connected = isCallConnected(log.getStatus());
            if (connected) {
                outConn++;
            }

            Long durationSec = outboundTalkDurationSeconds(log);
            if (durationSec != null && durationSec > 0) {
                outDurSum += durationSec;
                outDurCount++;
            }
        }

        LocalDateTime lastAttempt = logs.stream()
                .map(CallLogResponse::getCreatedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        LocalDateTime lastConnected = logs.stream()
                .filter(l -> isCallConnected(l.getStatus()))
                .map(CallLogResponse::getCreatedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return Lead.CallSummaryDetails.builder()
                .totalOutboundCalls(outTotal)
                .outboundConnectedCalls(outConn)
                .bestTimeToCall(hourRangeFromModalHour(modeHour(completedInboundOutboundCallHours(logs))))
                .lastConnectedCallAt(lastConnected)
                .lastCallAttemptAt(lastAttempt)
                .averageOutboundTalkDurationSeconds(avg(outDurSum, outDurCount))
                .consecutiveNoAnswers(countConsecutiveOutboundNoAnswers(logs))
                .build();
    }

    private static int countConsecutiveOutboundNoAnswers(List<CallLogResponse> logs) {
        List<CallLogResponse> outboundDesc = logs.stream()
                .filter(l -> l.getDirection() == CallDirection.OUTBOUND)
                .filter(l -> l.getCreatedAt() != null)
                .sorted(Comparator.comparing(CallLogResponse::getCreatedAt).reversed())
                .collect(Collectors.toList());

        int count = 0;
        for (CallLogResponse log : outboundDesc) {
            if (log.getStatus() == CallStatus.NO_ANSWER) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    private static boolean isCallConnected(CallStatus status) {
        return status == CallStatus.COMPLETED;
    }

    /**
     * Best time to call: modal clock hour from inbound and outbound calls that are {@link CallStatus#COMPLETED}.
     * Hour is taken from {@link com.nivasafinance.features.call.entity.CallLog.CompletionDetails#getStartTime()}
     * when present; otherwise {@link CallLogResponse#getCreatedAt()}.
     */
    private static List<Integer> completedInboundOutboundCallHours(List<CallLogResponse> logs) {
        List<Integer> hours = new ArrayList<>();
        for (CallLogResponse log : logs) {
            if (log.getStatus() != CallStatus.COMPLETED) {
                continue;
            }
            CallDirection d = log.getDirection();
            if (d != CallDirection.INBOUND && d != CallDirection.OUTBOUND) {
                continue;
            }
            LocalDateTime moment = null;
            if (log.getCompletionDetails() != null && log.getCompletionDetails().getStartTime() != null) {
                moment = log.getCompletionDetails().getStartTime();
            } else if (log.getCreatedAt() != null) {
                moment = log.getCreatedAt();
            }
            if (moment != null) {
                hours.add(moment.getHour());
            }
        }
        return hours;
    }

    private static Long outboundTalkDurationSeconds(CallLogResponse log) {
        if (log.getDirection() != CallDirection.OUTBOUND) {
            return null;
        }
        if (log.getCompletionDetails() == null || log.getCompletionDetails().getDuration() == null) {
            return null;
        }
        return log.getCompletionDetails().getDuration();
    }

    private static Double avg(long sum, int count) {
        return count > 0 ? (double) sum / count : null;
    }

    private static Integer modeHour(List<Integer> hours) {
        if (hours.isEmpty()) {
            return null;
        }
        int[] counts = new int[24];
        for (int h : hours) {
            if (h >= 0 && h < 24) {
                counts[h]++;
            }
        }
        int bestCount = -1;
        int bestHour = 0;
        for (int h = 0; h < 24; h++) {
            if (counts[h] > bestCount) {
                bestCount = counts[h];
                bestHour = h;
            }
        }
        return bestHour;
    }

    private static Lead.CallTimeHourRange hourRangeFromModalHour(Integer hour) {
        if (hour == null) {
            return null;
        }
        LocalTime start = LocalTime.of(hour, 0, 0);
        LocalTime end = hour < 23 ? LocalTime.of(hour + 1, 0, 0) : LocalTime.MAX;
        return Lead.CallTimeHourRange.builder()
                .start(start)
                .end(end)
                .build();
    }
}
