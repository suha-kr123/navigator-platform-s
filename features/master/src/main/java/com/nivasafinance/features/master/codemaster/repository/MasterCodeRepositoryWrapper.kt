package com.nivasafinance.features.master.codemaster.repository

import com.nivasafinance.features.master.codemaster.entity.MasterCode
import com.nivasafinance.features.master.codemaster.exception.CodeMasterExceptionFactory
import com.nivasafinance.features.master.codemaster.exception.CodeMasterNotFoundException
import org.springframework.context.MessageSource
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class MasterCodeRepositoryWrapper(
    private val masterCodeRepository: MasterCodeRepository,
    private val messageSource: MessageSource
) {

    fun saveWithException(masterCode: MasterCode): MasterCode {
        return try {
            masterCodeRepository.save(masterCode)
        } catch (e: DataAccessException) {
            throw CodeMasterExceptionFactory.createFailed(messageSource).apply { initCause(e) }
        }
    }

    fun findByIdWithException(id: UUID): MasterCode {
        return try {
            masterCodeRepository.findById(id).orElseThrow {
                CodeMasterExceptionFactory.notFoundById(id, messageSource)
            }
        } catch (e: CodeMasterNotFoundException) {
            throw e
        } catch (e: DataAccessException) {
            throw CodeMasterExceptionFactory.retrieveEntityFailed(messageSource).apply { initCause(e) }
        }
    }

    fun findByKeyWithException(key: String): MasterCode {
        return try {
            masterCodeRepository.findByKey(key).orElseThrow {
                CodeMasterExceptionFactory.notFound(key, messageSource)
            }
        } catch (e: CodeMasterNotFoundException) {
            throw e
        } catch (e: DataAccessException) {
            throw CodeMasterExceptionFactory.retrieveEntityFailed(messageSource).apply { initCause(e) }
        }
    }

    fun findByParentIdWithException(parentId: UUID): List<MasterCode> {
        return try {
            masterCodeRepository.findByParentId(parentId)
        } catch (e: DataAccessException) {
            throw CodeMasterExceptionFactory.retrieveEntityFailed(messageSource).apply { initCause(e) }
        }
    }

    fun findAllWithException(): List<MasterCode> {
        return try {
            masterCodeRepository.findAll()
        } catch (e: DataAccessException) {
            throw CodeMasterExceptionFactory.retrieveEntityFailed(messageSource).apply { initCause(e) }
        }
    }

    fun deleteByIdWithException(id: UUID) {
        if (!masterCodeRepository.existsById(id)) {
            throw CodeMasterExceptionFactory.notFoundById(id, messageSource)
        }
        try {
            masterCodeRepository.deleteById(id)
        } catch (e: DataAccessException) {
            throw CodeMasterExceptionFactory.deleteFailed(messageSource).apply { initCause(e) }
        }
    }
}
