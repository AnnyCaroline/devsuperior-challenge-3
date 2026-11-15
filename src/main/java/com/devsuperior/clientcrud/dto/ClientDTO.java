package com.devsuperior.clientcrud.dto;

import java.time.LocalDate;

import com.devsuperior.clientcrud.entities.Client;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

public record ClientDTO(
        Long id,
        @NotBlank(message = "Informe o nome do cliente.") String name,
        String cpf,
        Double income,
        @PastOrPresent(message = "A data de nascimento não pode estar no futuro.") LocalDate birthDate,
        Integer children) {

    public ClientDTO(Client client) {
        this(client.getId(), client.getName(), client.getCpf(), client.getIncome(),
                client.getBirthDate(), client.getChildren());
    }
}
