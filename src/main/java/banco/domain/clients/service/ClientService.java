package banco.domain.clients.service;

import banco.domain.clients.model.Client;

import java.util.List;

public interface ClientService {

    public List<Client> findAll();

    public Client findById(Long id);

    public Client save(Client client);

    public Client update(Long id, Client client);

    public void delete(Long id);
}
