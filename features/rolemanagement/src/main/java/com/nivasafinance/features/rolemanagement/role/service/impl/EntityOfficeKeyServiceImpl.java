package com.nivasafinance.features.rolemanagement.role.service.impl;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.rolemanagement.role.service.EntityOfficeKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntityOfficeKeyServiceImpl implements EntityOfficeKeyService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public String getOfficeKey(EntityType entityType, UUID entityId) {
        if (entityType == null || entityId == null) {
            return null;
        }

        try {
            switch (entityType) {
                case LEAD:
                    return getOfficeKeyForLead(entityId);
                case ADVISOR:
                    return getOfficeKeyForAdvisor(entityId);
                default:
                    log.warn("Unsupported entity type: {}", entityType);
                    return null;
            }
        } catch (Exception e) {
            log.warn("Failed to get office key for entity type {} with id {}: {}", entityType, entityId, e.getMessage());
            return null;
        }
    }

    @Override
    @Deprecated
    public String getOfficeKeyForLead(UUID leadId) {
        try {
            Object leadRepositoryWrapper = applicationContext.getBean("leadRepositoryWrapper");
            java.lang.reflect.Method findByLeadIdentifierWithException = 
                    leadRepositoryWrapper.getClass().getMethod("findByLeadIdentifierWithException", UUID.class);
            Object lead = findByLeadIdentifierWithException.invoke(leadRepositoryWrapper, leadId);
            
            java.lang.reflect.Method getOfficeKey = lead.getClass().getMethod("getOfficeKey");
            return (String) getOfficeKey.invoke(lead);
        } catch (Exception e) {
            log.warn("Failed to get office key for lead {}: {}", leadId, e.getMessage());
            return null;
        }
    }

    @Override
    @Deprecated
    public String getOfficeKeyForAdvisor(UUID advisorId) {
        try {
            Object advisorRepositoryWrapper = applicationContext.getBean("advisorRepositoryWrapper");
            java.lang.reflect.Method findByIdentifierWithException = 
                    advisorRepositoryWrapper.getClass().getMethod("findByIdentifierWithException", UUID.class);
            Object advisor = findByIdentifierWithException.invoke(advisorRepositoryWrapper, advisorId);
            
            java.lang.reflect.Method getOfficeKey = advisor.getClass().getMethod("getOfficeKey");
            return (String) getOfficeKey.invoke(advisor);
        } catch (Exception e) {
            log.warn("Failed to get office key for advisor {}: {}", advisorId, e.getMessage());
            return null;
        }
    }
}

