package com.banque.accountservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "notification-service", url = "${feign.client.config.notification-service.url}")
public interface NotificationClient {

    @PostMapping("/api/notifications/account-created/{clientId}")
    void sendAccountCreationNotification(@PathVariable("clientId") Long clientId,
                                         @RequestParam("accountNumber") String accountNumber);

    @PostMapping("/api/notifications/deposit/{clientId}")
    void sendDepositNotification(@PathVariable("clientId") Long clientId,
                                 @RequestParam("accountNumber") String accountNumber,
                                 @RequestParam("amount") BigDecimal amount);

    @PostMapping("/api/notifications/withdrawal/{clientId}")
    void sendWithdrawalNotification(@PathVariable("clientId") Long clientId,
                                    @RequestParam("accountNumber") String accountNumber,
                                    @RequestParam("amount") BigDecimal amount);

    @PostMapping("/api/notifications/account-closed/{clientId}")
    void sendAccountClosureNotification(@PathVariable("clientId") Long clientId,
                                        @RequestParam("accountNumber") String accountNumber);
}