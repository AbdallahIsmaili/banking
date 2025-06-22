package com.securitybanking.transaction.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.securitybanking.transaction.dto.NotificationRequest;

@FeignClient(name = "notification-service", url = "http://localhost:8084/api/notifications")

public interface NotificationClient {

    @PostMapping("/account-created/{clientId}")
    void sendAccountCreationNotification(@PathVariable("clientId") String clientId,
            @RequestBody NotificationRequest request);

    @PostMapping
    void sendGenericNotification(@RequestBody NotificationRequest request);

}
