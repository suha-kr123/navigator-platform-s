package com.nivasafinance.features.staff.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.staff.dto.StaffTodayActivityResponse;
import com.nivasafinance.features.staff.dto.StaffTodayActivityResponse.CallStats;
import com.nivasafinance.features.staff.exception.StaffExceptionFactory;
import com.nivasafinance.features.staff.repository.StaffActivityRepositoryWrapper;
import com.nivasafinance.features.staff.service.StaffActivityService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StaffActivityServiceImpl implements StaffActivityService {

    private final StaffActivityRepositoryWrapper activityRepositoryWrapper;
    private final UserReadService userReadService;
    private final MessageSource messageSource;

    @Override
    public StaffTodayActivityResponse getTodayActivity() {
        String username = UserContext.getUsername();
        if (!StringUtils.hasText(username)) {
            throw StaffExceptionFactory.noCurrentUser(messageSource);
        }

        UserResponse user = userReadService.getUserByUsername(username);
        String phone = extractPrimaryPhone(user);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Map<String, Object> callStats = activityRepositoryWrapper.getCallStats(phone, startOfDay, endOfDay);
        long stageMoves = activityRepositoryWrapper.getStageMoveCount(username, startOfDay, endOfDay);

        return StaffTodayActivityResponse.builder()
                .date(today)
                .firstCallAt((LocalDateTime) callStats.get("firstCallAt"))
                .lastCallAt((LocalDateTime) callStats.get("lastCallAt"))
                .inbound((CallStats) callStats.get("inbound"))
                .outbound((CallStats) callStats.get("outbound"))
                .stageMoves(stageMoves)
                .build();
    }

    private String extractPrimaryPhone(UserResponse user) {
        if (user.getPersonResponse() == null) {
            throw StaffExceptionFactory.noCurrentUser(messageSource);
        }
        List<MobileNumberDetails> phones = user.getPersonResponse().getMobileNumbers();
        if (phones == null || phones.isEmpty()) {
            throw StaffExceptionFactory.noCurrentUser(messageSource);
        }
        return phones.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsPrimary()))
                .map(MobileNumberDetails::getNumber)
                .findFirst()
                .orElse(phones.get(0).getNumber());
    }
}
