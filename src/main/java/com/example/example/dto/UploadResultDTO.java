package com.example.example.dto;

public class UploadResultDTO {
    private String folderName;
    private String pdfId;
    private boolean success;
    private String message;

    public UploadResultDTO() {
    }

    public UploadResultDTO(String folderName, String pdfId, boolean success, String message) {
        this.folderName = folderName;
        this.pdfId = pdfId;
        this.success = success;
        this.message = message;
    }

    // Getters and Setters
    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getPdfId() {
        return pdfId;
    }

    public void setPdfId(String pdfId) {
        this.pdfId = pdfId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}