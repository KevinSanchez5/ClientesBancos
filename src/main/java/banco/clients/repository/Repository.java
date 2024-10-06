package banco.clients.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Repository <ID, T>{

    Flux<T> findAll();
    Mono<T> findById(ID id);
    Mono<T> save(T object);
    Mono<T> update(ID id, T object);
    Mono<Void>delete(ID id);
}
