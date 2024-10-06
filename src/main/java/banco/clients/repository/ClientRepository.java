package banco.clients.repository;

import banco.clients.model.Client;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ClientRepository extends Repository<UUID, Client> {

}
