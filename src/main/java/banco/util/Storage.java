package banco.util;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;

public interface Storage <T>{
    Flux<T> importFile(File file);

    Mono<Void> exportFile(File file, List<T> elemts);

}
