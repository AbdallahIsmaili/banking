package com.banque.accountservice.dto;


import lombok.Data;

@Data
public class AccountUpdateDTO {
    private Long id;
    private String accountNumber;
    private String accountType; // Si vous autorisez le changement de type de compte
    // Ajoutez d'autres champs modifiables selon vos besoins
}