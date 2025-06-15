package com.securitybanking.notification.dto;

public class NotificationRequest {
    private String clientId;
    private String recipient;
    private String subject;
    private String message;

    // Default constructor
    public NotificationRequest() {
    }

    // Constructor with all fields
    public NotificationRequest(String clientId, String recipient, String subject, String message) {
        this.clientId = clientId;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
    }

    // Getter methods
    public String getClientId() {
        return clientId;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    // Setter methods
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "clientId='" + clientId + '\'' +
                ", recipient='" + recipient + '\'' +
                ", subject='" + subject + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}