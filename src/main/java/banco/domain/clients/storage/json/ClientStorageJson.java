package banco.domain.clients.storage.json;

import banco.domain.clients.model.Client;
import banco.util.Storage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class ClientStorageJson implements Storage<Client> {

    private final Logger logger = LoggerFactory.getLogger(ClientStorageJson.class);
    private final ObjectMapper objectMapper;

    public ClientStorageJson() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Flux<Client> importFile(File file) {
        return Flux.defer(() -> {
            try (InputStream inputStream = new FileInputStream(file);
                 Reader reader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1)) {
                Client[] clients = objectMapper.readValue(reader, Client[].class);
                return Flux.fromArray(clients);
            } catch (IOException e) {
                logger.error("Error reading JSON file: {}", e.getMessage());
                return Flux.empty();
            }
        });
    }

    @Override
    public Mono<Void> exportFile(File file, List<Client> clients) {
        return Mono.fromRunnable(() -> {
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.ISO_8859_1)) {
                logger.info("Iniciando la escritura del archivo: {}", file.getPath());
                writer.write(objectMapper.writeValueAsString(clients));
                writer.newLine();
                logger.info("Finalizada la escritura del archivo: {}", file.getPath());
            } catch (IOException e) {
                logger.error("Error writing JSON file: {}", e.getMessage());
                throw new UncheckedIOException(e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

}
