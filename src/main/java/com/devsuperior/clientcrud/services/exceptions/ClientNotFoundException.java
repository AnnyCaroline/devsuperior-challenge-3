package com.devsuperior.clientcrud.services.exceptions;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(Long id) {
        super("Cliente não encontrado para o ID " + id + ".");
    }
}
