package com.nivasafinance.features.document.storage;

import java.io.InputStream;

public interface ContentRepository {
    String saveFile(InputStream inputStream, String documentPath);
    void deleteFile(String storageKey);
    InputStream fetchFile(String storageKey);
    String getSignedDownloadUrl(String storageKey, Long expiresIn);
}


