package com.nivasafinance.features.master.codemaster.repository;

import com.nivasafinance.features.master.codemaster.entity.MasterCode;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterExceptionFactory;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterNotFoundException;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterOperationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
    
    public MasterCode findByIdWithException(UUID id) {
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
    
    public List<MasterCode> findByParentIdWithException(UUID parentId) {
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
    
    public void deleteByIdWithException(UUID id) {
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
}

