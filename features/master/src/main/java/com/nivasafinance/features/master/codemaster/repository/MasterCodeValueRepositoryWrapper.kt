package com.nivasafinance.features.master.codemaster.repository

import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue
import com.nivasafinance.features.master.codemaster.exception.CodeMasterExceptionFactory
import com.nivasafinance.features.master.codemaster.exception.CodeMasterNotFoundException
import org.springframework.context.MessageSource
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class MasterCodeValueRepositoryWrapper(
    private val masterCodeValueRepository: MasterCodeValueRepository,
    private val messageSource: MessageSource
) {

    fun saveWithException(masterCodeValue: MasterCodeValue): MasterCodeValue {
        return try {
            masterCodeValueRepository.save(masterCodeValue)
        } catch (e: DataAccessException) {
            throw CodeMasterExceptionFactory.createFailed(messageSource).apply { initCause(e) }
        }
    }

    fun findByIdWithException(id: UUID): MasterCodeValue {
        return try {
            masterCodeValueRepository.findById(id).orElseThrow {
                CodeMasterExceptionFactory.notFoundById(id, messageSource)
            }
        } catch (e: CodeMasterNotFoundException) {
            throw e
        } catch (e: DataAccessException) {
            throw CodeMasterExceptionFactory.retrieveEntityFailed(messageSource).apply { initCause(e) }
        }
    }

    fun findByKeyAndCodeKeyWithException(key: String, codeKey: String): MasterCodeValue {
        return try {
            masterCodeValueRepository.findByKeyAndCodeKey(key, codeKey).orElseThrow {
                CodeMasterExceptionFactory.notFound("Key: $key, CodeKey: $codeKey", messageSource)
            }
        } catch (e: CodeMasterNotFoundException) {
            throw e
        } catch (e: DataAccessException) {
            throw CodeMasterExceptionFactory.retrieveEntityFailed(messageSource).apply { initCause(e) }
        }
    }

    fun findByCodeKeyWithException(codeKey: String): List<MasterCodeValue> {
        return try {
            masterCodeValueRepository.findByCodeKey(codeKey)
        } catch (e: DataAccessException) {
            throw CodeMasterExceptionFactory.retrieveEntityFailed(messageSource).apply { initCause(e) }
        }
    }

    fun findByCodeKeyAndIsActiveTrueWithException(codeKey: String): List<MasterCodeValue> {
        return try {
            masterCodeValueRepository.findByCodeKeyAndIsActiveTrue(codeKey)
        } catch (e: DataAccessException) {
            throw CodeMasterExceptionFactory.retrieveEntityFailed(messageSource).apply { initCause(e) }
        }
    }

    fun findAllWithException(): List<MasterCodeValue> {
        return try {
            masterCodeValueRepository.findAll()
        } catch (e: DataAccessException) {
            throw CodeMasterExceptionFactory.retrieveEntityFailed(messageSource).apply { initCause(e) }
        }
    }

    fun deleteByIdWithException(id: UUID) {
        if (!masterCodeValueRepository.existsById(id)) {
            throw CodeMasterExceptionFactory.notFoundById(id, messageSource)
        }
        try {
            masterCodeValueRepository.deleteById(id)
        } catch (e: DataAccessException) {
            throw CodeMasterExceptionFactory.deleteFailed(messageSource).apply { initCause(e) }
        }
    }

    fun findByKeyWithException(key: String): MasterCodeValue {
        return masterCodeValueRepository.findByKey(key).orElseThrow {
            CodeMasterExceptionFactory.codeValueKeyNotFound(key, messageSource)
        }
    }
}
