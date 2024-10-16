package banco.domain.clients.repository;


import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Repository <ID, T>{

    CompletableFuture<List<T>> findAll();
    CompletableFuture<T> findById(ID id);
    CompletableFuture<T> save(T object);
    CompletableFuture<T> update(ID id, T object);
    CompletableFuture<Void>delete(ID id);
}
