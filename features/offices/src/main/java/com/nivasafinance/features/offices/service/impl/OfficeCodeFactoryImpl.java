package com.nivasafinance.features.offices.service.impl;

import com.nivasafinance.features.offices.entity.Office;
import com.nivasafinance.features.offices.exception.OfficeNotFoundException;
import com.nivasafinance.features.offices.repository.OfficeRepository;
import com.nivasafinance.features.offices.service.OfficeCodeFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@AllArgsConstructor
@Slf4j
public class OfficeCodeFactoryImpl implements OfficeCodeFactory {

    private final OfficeRepository officeRepository;
    private final MessageSource messageSource;

    @Override
    public String generateOfficeCode(Long parentId) {
        if (parentId == null) {
            // Root office - generate next available root code (001, 002, 003, etc.)
            return generateRootOfficeCode();
        } else {
            // Child office - generate code based on parent's code
            return generateChildOfficeCode(parentId);
        }
    }

    private String generateRootOfficeCode() {
        // Find the highest root office code and increment it
        List<Office> rootOffices = officeRepository.findByParentIdIsNullOrderByCodeDesc();
        int nextNumber;
        if (rootOffices.isEmpty()) {
            nextNumber = 1;
        } else {
            String lastCode = rootOffices.get(0).getCode();
            try {
                nextNumber = Integer.parseInt(lastCode) + 1;
            } catch (NumberFormatException e) {
                nextNumber = 1;
            }
        }
        return String.format(Locale.US, "%03d", nextNumber);
    }

    private String generateChildOfficeCode(Long parentId) {
        
        Office parentOffice = officeRepository.findById(parentId).orElseThrow(() -> {
            log.error("Parent office not found with id: {}", parentId);
            return new OfficeNotFoundException(String.valueOf(parentId), messageSource);
        });

        // Find all children of this parent and get the next sequence number
        List<Office> children = officeRepository.findByParentIdOrderByCodeDesc(parentId);
        String parentCode = parentOffice.getCode();

        int nextNumber;
        if (children.isEmpty()) {
            nextNumber = 1;
        } else {
            String lastChildCode = children.get(0).getCode();
            String lastSequenceStr = lastChildCode.substring(lastChildCode.lastIndexOf(".") + 1);
            try {
                nextNumber = Integer.parseInt(lastSequenceStr) + 1;
            } catch (NumberFormatException e) {
                log.warn("Failed to parse child code sequence from: {}", lastChildCode);
                nextNumber = 1;
            }
        }

        String generatedCode = parentCode + "." + String.format(Locale.US, "%03d", nextNumber);
        return generatedCode;
    }
}

