// account-service/src/main/java/com/banque/accountservice/controller/ClientController.java
package com.banque.accountservice.controller;

import com.banque.accountservice.dto.ClientCreationDTO;
import com.banque.accountservice.model.Client;
import com.banque.accountservice.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "http://localhost:8080")
public class ClientController {
    @Autowired
    private ClientService clientService;

    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody ClientCreationDTO dto) {
        Client client = clientService.createClient(dto);
        return ResponseEntity.ok(client);
    }
}