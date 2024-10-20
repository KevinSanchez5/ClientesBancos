package banco.util;

import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;

public interface Storage <T>{
    Mono<T> importFile(File file);

    Mono<Void> exportFile(File file, List<T> elemts);

}
