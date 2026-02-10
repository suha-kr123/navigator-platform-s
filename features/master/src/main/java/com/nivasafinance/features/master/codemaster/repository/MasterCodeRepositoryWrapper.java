package com.nivasafinance.features.master.codemaster.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeResponse;
import com.nivasafinance.features.master.codemaster.entity.MasterCode;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterExceptionFactory;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterNotFoundException;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterOperationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasterCodeRepositoryWrapper {
    
    private final MasterCodeRepository masterCodeRepository;
    private final MessageSource messageSource;
    private final CodeMasterExceptionFactory codeMasterExceptionFactory;
    
    public MasterCodeRepositoryWrapper(MasterCodeRepository masterCodeRepository, MessageSource messageSource) {
        this.masterCodeRepository = masterCodeRepository;
        this.messageSource = messageSource;
        this.codeMasterExceptionFactory = new CodeMasterExceptionFactory(messageSource);
    }
    
    public MasterCode saveWithException(MasterCode masterCode) {
        try {
            return masterCodeRepository.save(masterCode);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.createFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public MasterCode findByIdWithException(Long id) {
        try {
            return masterCodeRepository.findById(id).orElseThrow(() ->
                    codeMasterExceptionFactory.notFoundById(id, messageSource));
        } catch (CodeMasterNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public MasterCode findByKeyWithException(String key) {
        try {
            return masterCodeRepository.findByKey(key).orElseThrow(() ->
                    codeMasterExceptionFactory.notFound(key, messageSource));
        } catch (CodeMasterNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public List<MasterCode> findByParentIdWithException(Long parentId) {
        try {
            return masterCodeRepository.findByParentId(parentId);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public List<MasterCode> findAllWithException() {
        try {
            return masterCodeRepository.findAll();
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public void deleteByIdWithException(Long id) {
        if (!masterCodeRepository.existsById(id)) {
            throw codeMasterExceptionFactory.notFoundById(id, messageSource);
        }
        try {
            masterCodeRepository.deleteById(id);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.deleteFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public PaginatedResponse<MasterCode> findAllWithException(PaginationRequest request) {
        try {
            Pageable pageable = buildPageable(request);
            Page<MasterCode> page = masterCodeRepository.findAll(pageable);
            return buildPaginatedResponse(page, request);
        } catch (DataAccessException e) {
            throw buildRetrieveException(e);
        }
    }

    private Pageable buildPageable(PaginationRequest request) {
        int offset = request.getOffset();
        int limit = request.getLimit();
    
        String sortBy = request.getSortBy();
        String direction = request.getSortDirection();
    
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        return PageRequest.of(offset / limit, limit, sort);
    }
    
    private PaginatedResponse<MasterCode> buildPaginatedResponse(
            Page<MasterCode> page,
            PaginationRequest request
    ) {
        PaginationInfo pageInfo = new PaginationInfo(
                request.getOffset(),
                request.getLimit(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.hasNext(),
                page.hasPrevious()
        );
    
        return new PaginatedResponse<>(page.getContent(), pageInfo);
    }
    
    private CodeMasterOperationException buildRetrieveException(Exception e) {
        CodeMasterOperationException ex =
                codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
        ex.initCause(e);
        return ex;
    }
    
    
    
}

