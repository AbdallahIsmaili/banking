package com.securitybanking.transaction.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.securitybanking.transaction.dto.EmailRequest;

@FeignClient(name = "notification-service", url = "${notification.service.url}")
public interface NotificationClient {

    @PostMapping("/api/notifications/email")
    void sendEmail(@RequestBody EmailRequest emailRequest);
}