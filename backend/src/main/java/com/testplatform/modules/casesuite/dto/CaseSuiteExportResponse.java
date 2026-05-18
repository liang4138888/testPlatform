package com.testplatform.modules.casesuite.dto;

public class CaseSuiteExportResponse {

    private final Long exportedFileId;
    private final String fileName;

    public CaseSuiteExportResponse(Long exportedFileId, String fileName) {
        this.exportedFileId = exportedFileId;
        this.fileName = fileName;
    }

    public Long getExportedFileId() {
        return exportedFileId;
    }

    public String getFileName() {
        return fileName;
    }
}
