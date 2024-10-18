package banco.domain.clients.services.clientes;

import banco.domain.clients.model.Client;
import banco.domain.clients.services.cache.Cache;

public interface ClientesCache extends Cache<Long, Client> {
}
