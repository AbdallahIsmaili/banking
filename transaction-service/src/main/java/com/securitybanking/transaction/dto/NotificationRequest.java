package com.securitybanking.transaction.dto;

public class NotificationRequest {

    private String clientId;
    private String recipient;
    private String subject;
    private String message;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationRequest(String clientId, String recipient, String subject, String message) {
        this.clientId = clientId;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
    }

    public NotificationRequest() {
    }

}
