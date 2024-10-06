package banco.clients.repository;

import banco.clients.model.Client;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class ImplClientRepository implements ClientRepository {
    @Override
    public Flux<Client> findAll() {
        return null;
    }

    @Override
    public Mono<Client> findById(UUID id) {
        return null;
    }

    @Override
    public Mono<Client> save(Client object) {
        return null;
    }

    @Override
    public Mono<Client> update(UUID id, Client object) {
        return null;
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return null;
    }
}
