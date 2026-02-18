package com.nivasafinance.features.master.codemaster.repository;

import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterExceptionFactory;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterNotFoundException;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterOperationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MasterCodeValueRepositoryWrapper {
    
    private final MasterCodeValueRepository masterCodeValueRepository;
    private final MessageSource messageSource;
    private final CodeMasterExceptionFactory codeMasterExceptionFactory;
    
    public MasterCodeValueRepositoryWrapper(MasterCodeValueRepository masterCodeValueRepository, MessageSource messageSource) {
        this.masterCodeValueRepository = masterCodeValueRepository;
        this.messageSource = messageSource;
        this.codeMasterExceptionFactory = new CodeMasterExceptionFactory(messageSource);
    }
    
    public MasterCodeValue saveWithException(MasterCodeValue masterCodeValue) {
        try {
            return masterCodeValueRepository.save(masterCodeValue);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.createFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public MasterCodeValue findByIdWithException(Long id) {
        try {
            return masterCodeValueRepository.findById(id).orElseThrow(() ->
                    codeMasterExceptionFactory.notFoundById(id, messageSource));
        } catch (CodeMasterNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public MasterCodeValue findByKeyAndCodeKeyWithException(String key, String codeKey) {
        try {
            return masterCodeValueRepository.findByKeyAndCodeKey(key, codeKey).orElseThrow(() ->
                    codeMasterExceptionFactory.notFound("Key: " + key + ", CodeKey: " + codeKey, messageSource));
        } catch (CodeMasterNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public List<MasterCodeValue> findByCodeKeyWithException(String codeKey) {
        try {
            return masterCodeValueRepository.findByCodeKey(codeKey);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public List<MasterCodeValue> findByCodeKeyAndIsActiveTrueWithException(String codeKey) {
        try {
            return masterCodeValueRepository.findByCodeKeyAndIsActiveTrue(codeKey);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public List<MasterCodeValue> findAllWithException() {
        try {
            return masterCodeValueRepository.findAll();
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public void deleteByIdWithException(Long id) {
        if (!masterCodeValueRepository.existsById(id)) {
            throw codeMasterExceptionFactory.notFoundById(id, messageSource);
        }
        try {
            masterCodeValueRepository.deleteById(id);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.deleteFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public MasterCodeValue findByKeyWithException(String key) {
        return masterCodeValueRepository.findByKey(key).orElseThrow(() ->
                codeMasterExceptionFactory.codeValueKeyNotFound(key, messageSource));
    }

    public Set<String> findAllKeysWithException() {
        try {
            return masterCodeValueRepository.findAll().stream()
                    .map(MasterCodeValue::getKey)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (DataAccessException e) {
            throw codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public List<MasterCodeValue> saveAllWithException(List<MasterCodeValue> masterCodeValues) {
        try {
            String masterCodeKey = masterCodeValues.get(0).getCodeKey();
            masterCodeValueRepository.saveAll(masterCodeValues);
            return findByCodeKeyWithException(masterCodeKey);
        } catch (DataAccessException e) {
            throw codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }
}   

