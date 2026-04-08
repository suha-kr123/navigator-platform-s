package com.nivasafinance.features.admin.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.admin.exception.AdminExceptionFactory;
import com.nivasafinance.features.admin.service.AdminCascadeService;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import com.nivasafinance.features.lead.dto.AdminLeadSearchResponse;
import com.nivasafinance.features.lead.dto.LeadSearchRequest;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.person.service.PersonWriteService;
import com.nivasafinance.features.staff.entity.Staff;
import com.nivasafinance.features.staff.service.StaffReadService;
import com.nivasafinance.features.staff.service.StaffWriteService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.usermanagement.service.UserWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCascadeServiceImpl implements AdminCascadeService {

    private static final String LOG_CASCADE_DELETED_PERSON = "Cascade deleted person with mobile {}";
    private static final String LOG_CASCADE_RESTORED_PERSON = "Cascade restored person with mobile {}";
    private static final String LOG_CASCADE_DELETED_USER = "Cascade deleted user {}";
    private static final String LOG_CASCADE_RESTORED_USER = "Cascade restored user {}";
    private static final String LOG_CASCADE_DELETED_STAFF = "Cascade deleted staff {} for user {}";
    private static final String LOG_CASCADE_RESTORED_STAFF = "Cascade restored staff {} for user {}";
    private static final String LOG_CASCADE_DELETED_ADVISOR = "Cascade deleted advisor {} for user {}";
    private static final String LOG_CASCADE_RESTORED_ADVISOR = "Cascade restored advisor {} for user {}";
    private static final String LOG_CASCADE_DELETED_LEAD = "Cascade deleted lead {} for mobile {}";
    private static final String LOG_CASCADE_RESTORED_LEAD = "Cascade restored lead {} for mobile {}";

    private final MessageSource messageSource;
    private final PersonWriteService personWriteService;
    private final UserReadService userReadService;
    private final UserWriteService userWriteService;
    private final StaffReadService staffReadService;
    private final StaffWriteService staffWriteService;
    private final AdvisorReadService advisorReadService;
    private final AdvisorWriteService advisorWriteService;
    private final LeadReadService leadReadService;
    private final LeadWriteService leadWriteService;

    @Override
    @Transactional
    public void cascadeDeletePerson(String mobileNumber) {
        // 1. Delete all leads where this person is the primary contact
        cascadeDeleteLeadsForMobile(mobileNumber);

        // 2. Find linked user by person's mobile (raw entity to avoid loading Person into session)
        List<User> users = userReadService.findUsersByPersonPhoneNumber(mobileNumber);
        if (!users.isEmpty()) {
            User user = users.get(0);
            // 3. Cascade delete user → staff/advisor
            cascadeDeleteUserInternalFromEntity(user);
        }

        // 4. Delete person
        personWriteService.deletePerson(mobileNumber);
        log.info(LOG_CASCADE_DELETED_PERSON, mobileNumber);
    }

    @Override
    @Transactional
    public void cascadeRestorePerson(String mobileNumber) {
        // 1. Restore person first
        personWriteService.undoDeletePerson(mobileNumber);

        // 2. Find linked user (raw entity to avoid loading Person into session)
        List<User> users = userReadService.findUsersByPersonPhoneNumber(mobileNumber);
        if (!users.isEmpty()) {
            User user = users.get(0);
            cascadeRestoreUserInternalFromEntity(user);
        }

        // 3. Restore all leads where this person is the primary contact
        cascadeRestoreLeadsForMobile(mobileNumber);

        log.info(LOG_CASCADE_RESTORED_PERSON, mobileNumber);
    }

    @Override
    @Transactional
    public void cascadeDeleteUser(String username) {
        UserResponse user = userReadService.adminGetUserByUsername(username);
        cascadeDeleteUserInternal(user);
        log.info(LOG_CASCADE_DELETED_USER, username);
    }

    @Override
    @Transactional
    public void cascadeRestoreUser(String username) {
        UserResponse user = userReadService.adminGetUserByUsername(username);
        if (user.getPersonResponse() == null) {
            throw AdminExceptionFactory.cannotRestoreUserPersonDeleted(messageSource);
        }
        cascadeRestoreUserInternal(user);
        log.info(LOG_CASCADE_RESTORED_USER, username);
    }

    private void cascadeDeleteUserInternal(UserResponse user) {
        cascadeDeleteUserByFields(user.getUsername(), user.getId(), user.getDeleted());
    }

    private void cascadeDeleteUserInternalFromEntity(User user) {
        cascadeDeleteUserByFields(user.getUsername(), user.getId(), user.getIsDeleted());
    }

    private void cascadeDeleteUserByFields(String username, Long userId, Boolean deleted) {
        // 1. Delete staff if exists
        Optional<Staff> staffOpt = staffReadService.findStaffByUserIdIncludingDeleted(userId);
        if (staffOpt.isPresent() && !Boolean.TRUE.equals(staffOpt.get().getIsDeleted())) {
            staffWriteService.deleteStaff(staffOpt.get().getIdentifier());
            log.info(LOG_CASCADE_DELETED_STAFF, staffOpt.get().getIdentifier(), username);
        }

        // 2. Delete advisor if exists
        Optional<Advisor> advisorOpt = advisorReadService.findAdvisorByUsername(username);
        if (advisorOpt.isPresent() && !Boolean.TRUE.equals(advisorOpt.get().getIsDeleted())) {
            advisorWriteService.deleteAdvisor(advisorOpt.get().getIdentifier());
            log.info(LOG_CASCADE_DELETED_ADVISOR, advisorOpt.get().getIdentifier(), username);
        }

        // 3. Delete user
        if (!Boolean.TRUE.equals(deleted)) {
            userWriteService.deleteUser(username);
        }
    }

    private void cascadeRestoreUserInternal(UserResponse user) {
        cascadeRestoreUserByFields(user.getUsername(), user.getId(), user.getDeleted());
    }

    private void cascadeRestoreUserInternalFromEntity(User user) {
        cascadeRestoreUserByFields(user.getUsername(), user.getId(), user.getIsDeleted());
    }

    private void cascadeRestoreUserByFields(String username, Long userId, Boolean deleted) {
        // 1. Restore user first
        if (Boolean.TRUE.equals(deleted)) {
            userWriteService.undoDeleteUser(username);
        }

        // 2. Restore staff if exists
        Optional<Staff> staffOpt = staffReadService.findStaffByUserIdIncludingDeleted(userId);
        if (staffOpt.isPresent() && Boolean.TRUE.equals(staffOpt.get().getIsDeleted())) {
            staffWriteService.undoDeleteStaff(staffOpt.get().getIdentifier());
            log.info(LOG_CASCADE_RESTORED_STAFF, staffOpt.get().getIdentifier(), username);
        }

        // 3. Restore advisor if exists (use IncludingDeleted since advisor is soft-deleted)
        Optional<Advisor> advisorOpt = advisorReadService.findAdvisorByUsernameIncludingDeleted(username);
        if (advisorOpt.isPresent() && Boolean.TRUE.equals(advisorOpt.get().getIsDeleted())) {
            advisorWriteService.undoDeleteAdvisor(advisorOpt.get().getIdentifier());
            log.info(LOG_CASCADE_RESTORED_ADVISOR, advisorOpt.get().getIdentifier(), username);
        }
    }

    private static final int CASCADE_BATCH_SIZE = 500;

    private void cascadeDeleteLeadsForMobile(String mobileNumber) {
        LeadSearchRequest searchRequest = new LeadSearchRequest();
        searchRequest.setMobileNumber(mobileNumber);
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setLimit(CASCADE_BATCH_SIZE);

        int offset = 0;
        List<AdminLeadSearchResponse> content;
        do {
            paginationRequest.setOffset(offset);
            PaginatedResponse<AdminLeadSearchResponse> leads = leadReadService.adminSearchLeads(paginationRequest, searchRequest);
            content = leads.getContent();

            if (content != null) {
                for (AdminLeadSearchResponse lead : content) {
                    if (!Boolean.TRUE.equals(lead.getDeleted())) {
                        leadWriteService.deleteLead(lead.getLeadIdentifier());
                        log.info(LOG_CASCADE_DELETED_LEAD, lead.getLeadIdentifier(), mobileNumber);
                    }
                }
            }
            offset += CASCADE_BATCH_SIZE;
        } while (content != null && content.size() == CASCADE_BATCH_SIZE);
    }

    private void cascadeRestoreLeadsForMobile(String mobileNumber) {
        LeadSearchRequest searchRequest = new LeadSearchRequest();
        searchRequest.setMobileNumber(mobileNumber);
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setLimit(CASCADE_BATCH_SIZE);

        int offset = 0;
        List<AdminLeadSearchResponse> content;
        do {
            paginationRequest.setOffset(offset);
            PaginatedResponse<AdminLeadSearchResponse> leads = leadReadService.adminSearchLeads(paginationRequest, searchRequest);
            content = leads.getContent();

            if (content != null) {
                for (AdminLeadSearchResponse lead : content) {
                    if (Boolean.TRUE.equals(lead.getDeleted())) {
                        leadWriteService.undoDeleteLead(lead.getLeadIdentifier());
                        log.info(LOG_CASCADE_RESTORED_LEAD, lead.getLeadIdentifier(), mobileNumber);
                    }
                }
            }
            offset += CASCADE_BATCH_SIZE;
        } while (content != null && content.size() == CASCADE_BATCH_SIZE);
    }
}
