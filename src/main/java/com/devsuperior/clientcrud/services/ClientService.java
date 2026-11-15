package com.devsuperior.clientcrud.services;

import com.devsuperior.clientcrud.dto.ClientDTO;
import com.devsuperior.clientcrud.entities.Client;
import com.devsuperior.clientcrud.repositories.ClientRepository;
import com.devsuperior.clientcrud.services.exceptions.ClientNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<ClientDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(ClientDTO::new);
    }

    @Transactional(readOnly = true)
    public ClientDTO findById(Long id) {
        return new ClientDTO(getClient(id));
    }

    @Transactional
    public ClientDTO insert(ClientDTO dto) {
        Client client = new Client(dto.name(), dto.cpf(), dto.income(), dto.birthDate(), dto.children());
        return new ClientDTO(repository.save(client));
    }

    @Transactional
    public ClientDTO update(Long id, ClientDTO dto) {
        Client client = getClient(id);
        client.update(dto.name(), dto.cpf(), dto.income(), dto.birthDate(), dto.children());
        return new ClientDTO(client);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getClient(id));
    }

    private Client getClient(Long id) {
        return repository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));
    }
}
