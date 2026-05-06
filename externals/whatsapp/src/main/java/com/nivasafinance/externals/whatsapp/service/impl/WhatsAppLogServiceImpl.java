package com.nivasafinance.externals.whatsapp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.nivasafinance.common.utils.PhoneNumberUtils;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLogRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLogResponse;
import com.nivasafinance.externals.whatsapp.service.WhatsAppLogService;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.whatsapp.entity.WhatsappChannel;
import com.nivasafinance.features.whatsapp.entity.WhatsappLog;
import com.nivasafinance.features.whatsapp.entity.WhatsappLogAdvisor;
import com.nivasafinance.features.whatsapp.entity.WhatsappLogLead;
import com.nivasafinance.features.whatsapp.enums.WhatsappCreatedSource;
import com.nivasafinance.features.whatsapp.enums.WhatsappEntity;
import com.nivasafinance.features.whatsapp.enums.WhatsappMessageType;
import com.nivasafinance.features.whatsapp.enums.WhatsappSentBy;
import com.nivasafinance.features.whatsapp.enums.WhatsappStatus;
import com.nivasafinance.features.whatsapp.repository.WhatsappChannelRepository;
import com.nivasafinance.features.whatsapp.repository.WhatsappLogAdvisorRepository;
import com.nivasafinance.features.whatsapp.repository.WhatsappLogLeadRepository;
import com.nivasafinance.features.whatsapp.repository.WhatsappLogRepository;
import com.nivasafinance.notification.orchestrator.repository.AdvisorWhatsAppNotificationRepository;
import com.nivasafinance.notification.orchestrator.repository.LeadWhatsAppNotificationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class WhatsAppLogServiceImpl implements WhatsAppLogService {

    private static final String PROVIDER = "GALLABOX";

    private final WhatsappLogRepository whatsappLogRepository;
    private final WhatsappLogLeadRepository whatsappLogLeadRepository;
    private final WhatsappLogAdvisorRepository whatsappLogAdvisorRepository;
    private final WhatsappChannelRepository whatsappChannelRepository;
    private final LeadWhatsAppNotificationRepository leadWhatsAppNotificationRepository;
    private final AdvisorWhatsAppNotificationRepository advisorWhatsAppNotificationRepository;
    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public WhatsAppLogResponse createWhatsAppLog(WhatsAppLogRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("WhatsAppLogRequest must not be null");
        }
        log.info("Creating whatsapp log: providerMessageId={}, channelId={}, conversationId={}, localMessageId={}",
                request.getId(), request.getChannelId(), request.getConversationId(), request.getLocalMessageId());

        try {
            JsonNode whatsapp = request.getWhatsapp();

            WhatsappChannel channel = whatsappChannelRepository.findByChannelId(request.getChannelId()).orElse(null);
            if (channel == null) {
                log.warn("WhatsApp channel not found for channelId={}; log will be saved without entity linkage",
                        request.getChannelId());
            }
            WhatsappEntity entityType = channel != null ? channel.getEntity() : null;

            WhatsappStatus status = resolveStatus(whatsapp);
            WhatsappMessageType messageType = resolveMessageType(whatsapp);
            String phone = resolvePhone(whatsapp);
            LocalDateTime messageTime = resolveMessageTime(whatsapp);
            WhatsappLog.TemplateDetails templateDetails = resolveTemplateDetails(whatsapp, messageType);
            WhatsappLog.MessageData messageData = resolveMessageData(whatsapp, messageType);

            WhatsappCreatedSource createdSource = resolveCreatedSource(status, messageType, request.getLocalMessageId(), entityType);
            WhatsappSentBy sentBy = resolveSentBy(createdSource);

            WhatsappLog logEntity = WhatsappLog.builder()
                    .provider(PROVIDER)
                    .providerMessageId(request.getId())
                    .providerConversationId(request.getConversationId())
                    .phone(phone)
                    .fromChannelId(request.getChannelId())
                    .status(status)
                    .messageType(messageType)
                    .messageData(messageData)
                    .templateDetails(templateDetails)
                    .createdSource(createdSource)
                    .sentBy(sentBy)
                    .messageTime(messageTime)
                    .build();

            WhatsappLog saved = whatsappLogRepository.save(logEntity);
            log.info("Saved whatsapp log id={}, identifier={}, status={}, messageType={}, createdSource={}",
                    saved.getId(), saved.getIdentifier(), status, messageType, createdSource);

            linkToEntity(saved.getId(), entityType, phone);

            return WhatsAppLogResponse.builder()
                    .logIdentifier(saved.getIdentifier())
                    .build();
        } catch (DataAccessException e) {
            log.error("DB error creating whatsapp log for providerMessageId={}, channelId={}: {}",
                    request.getId(), request.getChannelId(), e.getMessage(), e);
            throw new RuntimeException("Failed to persist whatsapp log", e);
        } catch (RuntimeException e) {
            log.error("Unexpected error creating whatsapp log for providerMessageId={}, channelId={}: {}",
                    request.getId(), request.getChannelId(), e.getMessage(), e);
            throw e;
        }
    }

    private WhatsappStatus resolveStatus(JsonNode whatsapp) {
        if (whatsapp == null) {
            return null;
        }
        if (whatsapp.hasNonNull("from")) {
            return WhatsappStatus.RECEIVED;
        }
        if (whatsapp.hasNonNull("to")) {
            return WhatsappStatus.SENT;
        }
        return null;
    }

    private WhatsappMessageType resolveMessageType(JsonNode whatsapp) {
        if (whatsapp == null || !whatsapp.hasNonNull("type")) {
            return null;
        }
        String raw = whatsapp.get("type").asText();
        try {
            return WhatsappMessageType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unsupported whatsapp message type '{}', falling back to UNSUPPORTED", raw);
            return WhatsappMessageType.UNSUPPORTED;
        }
    }

    private String resolvePhone(JsonNode whatsapp) {
        if (whatsapp == null) {
            return null;
        }
        String raw = whatsapp.hasNonNull("from") ? whatsapp.get("from").asText()
                : whatsapp.hasNonNull("to") ? whatsapp.get("to").asText()
                : null;
        return PhoneNumberUtils.normalizePhoneNumber(raw);
    }

    private LocalDateTime resolveMessageTime(JsonNode whatsapp) {
        if (whatsapp == null || !whatsapp.hasNonNull("time")) {
            return null;
        }
        String raw = whatsapp.get("time").asText();
        try {
            return OffsetDateTime.parse(raw).toLocalDateTime();
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse whatsapp.time '{}': {}", raw, e.getMessage());
            return null;
        }
    }

    private WhatsappLog.TemplateDetails resolveTemplateDetails(JsonNode whatsapp, WhatsappMessageType messageType) {
        if (messageType != WhatsappMessageType.TEMPLATE || whatsapp == null || !whatsapp.hasNonNull("template")) {
            return null;
        }
        JsonNode template = whatsapp.get("template");
        String templateId = template.path("templateId").asText(null);
        String category = template.path("category").asText(null);
        String templateName = template.path("template").path("name").asText(null);
        return WhatsappLog.TemplateDetails.builder()
                .templateId(templateId)
                .templateName(templateName)
                .category(category)
                .build();
    }

    private WhatsappLog.MessageData resolveMessageData(JsonNode whatsapp, WhatsappMessageType messageType) {
        if (whatsapp == null || messageType == null) {
            return null;
        }
        if (messageType == WhatsappMessageType.TEXT) {
            String body = whatsapp.path("text").path("body").asText(null);
            return body == null ? null : WhatsappLog.MessageData.builder().text(body).build();
        }
        if (messageType == WhatsappMessageType.TEMPLATE) {
            return resolveTemplateMessageData(whatsapp.path("template"));
        }
        if (messageType == WhatsappMessageType.INTERACTIVE) {
            return resolveInteractiveMessageData(whatsapp.path("interactive"));
        }
        String mediaKey = switch (messageType) {
            case IMAGE -> "image";
            case VIDEO -> "video";
            case AUDIO -> "audio";
            case DOCUMENT -> "document";
            case STICKER -> "sticker";
            default -> null;
        };
        if (mediaKey == null || !whatsapp.hasNonNull(mediaKey)) {
            return null;
        }
        JsonNode media = whatsapp.get(mediaKey);
        WhatsappLog.MessageData.FileDetails fileDetails = WhatsappLog.MessageData.FileDetails.builder()
                .caption(media.path("caption").asText(null))
                .fileUrl(media.path("path").asText(null))
                .filetype(media.path("mimeType").asText(null))
                .fileName(media.path("filename").asText(null))
                .build();
        return WhatsappLog.MessageData.builder().fileDetails(fileDetails).build();
    }

    private WhatsappLog.MessageData resolveInteractiveMessageData(JsonNode interactive) {
        if (interactive == null || interactive.isMissingNode() || interactive.isNull()) {
            return null;
        }
        String buttonReplyTitle = interactive.path("button_reply").path("title").asText(null);
        if (buttonReplyTitle != null && !buttonReplyTitle.isBlank()) {
            return WhatsappLog.MessageData.builder().text(buttonReplyTitle).build();
        }
        String listReplyTitle = interactive.path("list_reply").path("title").asText(null);
        if (listReplyTitle != null && !listReplyTitle.isBlank()) {
            return WhatsappLog.MessageData.builder().text(listReplyTitle).build();
        }
        StringBuilder sb = new StringBuilder();
        String body = interactive.path("body").path("text").asText(null);
        if (body != null && !body.isBlank()) {
            sb.append(body);
        }
        JsonNode buttons = interactive.path("action").path("buttons");
        if (buttons.isArray()) {
            for (JsonNode button : buttons) {
                String title = button.path("reply").path("title").asText(null);
                if (title != null && !title.isBlank()) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(title);
                }
            }
        }
        if (sb.length() == 0) {
            return null;
        }
        return WhatsappLog.MessageData.builder().text(sb.toString()).build();
    }

    private WhatsappLog.MessageData resolveTemplateMessageData(JsonNode template) {
        if (template == null || template.isMissingNode() || template.isNull()) {
            return null;
        }
        String body = template.path("body").asText(null);
        WhatsappLog.MessageData.FileDetails fileDetails = resolveTemplateHeaderFileDetails(template.path("header"));
        if (body == null && fileDetails == null) {
            return null;
        }
        return WhatsappLog.MessageData.builder()
                .text(body)
                .fileDetails(fileDetails)
                .build();
    }

    private WhatsappLog.MessageData.FileDetails resolveTemplateHeaderFileDetails(JsonNode header) {
        if (header == null || header.isMissingNode() || header.isNull()) {
            return null;
        }
        JsonNode media = header.hasNonNull("image") ? header.get("image")
                : header.hasNonNull("video") ? header.get("video")
                : header.hasNonNull("document") ? header.get("document")
                : null;
        if (media == null) {
            return null;
        }
        return WhatsappLog.MessageData.FileDetails.builder()
                .fileUrl(media.path("path").asText(null))
                .filetype(media.path("mimeType").asText(null))
                .fileName(media.path("filename").asText(null))
                .build();
    }

    private WhatsappCreatedSource resolveCreatedSource(WhatsappStatus status, WhatsappMessageType messageType,
                                                       String localMessageId, WhatsappEntity entityType) {
        if (status != WhatsappStatus.SENT) {
            return null;
        }
        if (messageType != WhatsappMessageType.TEMPLATE) {
            return WhatsappCreatedSource.BOT;
        }
        if (entityType == null) {
            return null;
        }
        if (localMessageId == null || localMessageId.isBlank()) {
            return WhatsappCreatedSource.SEQUENCE;
        }
        boolean foundInApiNotifications = entityType == WhatsappEntity.LEAD
                ? leadWhatsAppNotificationRepository.findByLocalMessageId(localMessageId).isPresent()
                : advisorWhatsAppNotificationRepository.findByLocalMessageId(localMessageId).isPresent();
        return foundInApiNotifications ? WhatsappCreatedSource.API : WhatsappCreatedSource.SEQUENCE;
    }

    private WhatsappSentBy resolveSentBy(WhatsappCreatedSource createdSource) {
        if (createdSource == WhatsappCreatedSource.API) {
            return WhatsappSentBy.API;
        }
        if (createdSource == WhatsappCreatedSource.SEQUENCE) {
            return WhatsappSentBy.SEQUENCE;
        }
        return null;
    }

    private void linkToEntity(Long whatsappLogId, WhatsappEntity entityType, String mobileNumber) {
        if (entityType == null || mobileNumber == null || mobileNumber.isBlank()) {
            log.debug("Skipping entity link for whatsappLogId={}: entityType={}, mobileNumber blank? {}",
                    whatsappLogId, entityType, mobileNumber == null || mobileNumber.isBlank());
            return;
        }
        try {
            Optional<Person> person = personRepositoryWrapper.findByPrimaryMobileNumber(mobileNumber);
            if (person.isEmpty()) {
                log.info("No person found for mobile={}; whatsappLogId={} left unmapped", mobileNumber, whatsappLogId);
                return;
            }
            Long personId = person.get().getId();
            if (entityType == WhatsappEntity.LEAD) {
                Optional<Long> leadIdOpt = findLeadIdByPersonId(personId);
                if (leadIdOpt.isEmpty()) {
                    log.info("No lead found for personId={}; whatsappLogId={} left unmapped to lead",
                            personId, whatsappLogId);
                    return;
                }
                Long leadId = leadIdOpt.get();
                Long contactId = findContactIdByPersonId(personId).orElse(null);
                whatsappLogLeadRepository.save(new WhatsappLogLead(whatsappLogId, leadId, contactId));
                log.info("Linked whatsappLogId={} to leadId={} (contactId={})", whatsappLogId, leadId, contactId);
            } else if (entityType == WhatsappEntity.ADVISOR) {
                Optional<Long> advisorIdOpt = findAdvisorIdByPersonId(personId);
                if (advisorIdOpt.isEmpty()) {
                    log.info("No advisor found for personId={}; whatsappLogId={} left unmapped to advisor",
                            personId, whatsappLogId);
                    return;
                }
                Long advisorId = advisorIdOpt.get();
                whatsappLogAdvisorRepository.save(new WhatsappLogAdvisor(whatsappLogId, advisorId));
                log.info("Linked whatsappLogId={} to advisorId={}", whatsappLogId, advisorId);
            }
        } catch (DataAccessException e) {
            log.error("DB error linking whatsappLogId={} to entity {}: {}",
                    whatsappLogId, entityType, e.getMessage(), e);
            throw new RuntimeException("Failed to link whatsapp log to entity", e);
        }
    }

    private Optional<Long> findLeadIdByPersonId(Long personId) {
        String sql = """
                SELECT l.id
                FROM n_lead l
                JOIN LATERAL (
                    SELECT (contact_id)::bigint AS id
                    FROM jsonb_array_elements(l.contacts) AS contact_id
                ) contact_ids ON true
                JOIN n_contact c ON c.id = contact_ids.id
                WHERE c.person_id = ?
                ORDER BY l.updated_at DESC
                LIMIT 1
                """;
        return queryForOptionalLong(sql, personId);
    }

    private Optional<Long> findContactIdByPersonId(Long personId) {
        String sql = "SELECT id FROM n_contact WHERE person_id = ? ORDER BY updated_at DESC LIMIT 1";
        return queryForOptionalLong(sql, personId);
    }

    private Optional<Long> findAdvisorIdByPersonId(Long personId) {
        String sql = """
                SELECT a.id
                FROM n_advisor a
                JOIN n_user u ON u.username = a.username
                WHERE u.person_id = ?
                ORDER BY a.updated_at DESC
                LIMIT 1
                """;
        return queryForOptionalLong(sql, personId);
    }

    private Optional<Long> queryForOptionalLong(String sql, Object... args) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Long.class, args));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            log.warn("DB lookup failed (args={}): {}", args, e.getMessage());
            return Optional.empty();
        }
    }
}
