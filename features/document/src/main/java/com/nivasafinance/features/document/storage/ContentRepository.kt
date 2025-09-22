package com.nivasafinance.features.document.storage

import java.io.InputStream

interface ContentRepository {
    fun saveFile(inputStream: InputStream, documentPath: String): String
    fun deleteFile(storageKey: String)
    fun fetchFile(storageKey: String): InputStream
    fun getSignedDownloadUrl(storageKey: String, expiresIn: Long): String?
}
