package com.nivasafinance.features.document.storage

import java.io.InputStream

interface ContentRepository {
    fun saveFile(inputStream: InputStream, documentPath: String): String
    fun deleteFile(documentPath: String)
    fun fetchFile(documentPath: String): InputStream
    fun getSignedDownloadUrl(documentPath: String, expiresIn: Long): String?
}
