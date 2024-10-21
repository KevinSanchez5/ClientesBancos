package banco.domain.clients.storage.csv;

import banco.domain.clients.model.Client;
import banco.util.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

public class ClientStorageCsv implements Storage<Client> {
    private final Logger logger = LoggerFactory.getLogger(ClientStorageCsv.class);

    @Override
    public Flux<Client> importFile(File file) {
        return Flux.using(
                        () -> Files.newBufferedReader(file.toPath(), StandardCharsets.ISO_8859_1),
                        reader -> Flux.fromStream(reader.lines().skip(1)) // Skip header
                                .doOnNext(line -> logger.info("Processing line: " + line))
                                .map(line -> {
                                    String[] fields = line.split(",");
                                    Client client = new Client(
                                            Long.parseLong(fields[0]),
                                            fields[1],
                                            fields[2],
                                            fields[3]);
                                    client.setCreatedAt(LocalDateTime.parse(fields[4]));
                                    client.setUpdatedAt(LocalDateTime.parse(fields[5]));
                                    return client;
                                })
                                .doOnComplete(() -> logger.info("Completed processing lines")),
                        reader -> {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                logger.error("Error closing BufferedReader: {}", e.getMessage());
                            }
                        }
                ).subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> logger.error("Error reading CSV file: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> exportFile(File file, List<Client> clients) {
        return Mono.fromRunnable(() -> {
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.ISO_8859_1)) {
                logger.info("Iniciando la escritura del archivo CSV: {}", file.getPath());
                writer.write("id,name,username,email,createdAt,updatedAt");
                writer.newLine();
                for (Client client : clients) {
                    writer.write(String.format("%d,%s,%s,%s,%s,%s",
                            client.getId(),
                            client.getName(),
                            client.getUsername(),
                            client.getEmail(),
                            client.getCreatedAt(),
                            client.getUpdatedAt()));
                    writer.newLine();
                }
                logger.info("Finalizada la escritura del archivo CSV: {}", file.getPath());
            } catch (IOException e) {
                logger.error("Error writing CSV file: {}", e.getMessage());
                throw new UncheckedIOException(e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}