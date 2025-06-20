package com.banque.accountservice.service;

import com.banque.accountservice.dto.ClientCreationDTO;
import com.banque.accountservice.model.Client;

public interface ClientService {
    Client createClient(ClientCreationDTO dto);
}