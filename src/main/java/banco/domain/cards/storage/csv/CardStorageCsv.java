package banco.domain.cards.storage.csv;

import banco.domain.cards.model.BankCard;
import banco.util.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CardStorageCsv implements Storage<BankCard> {
    private final Logger logger = LoggerFactory.getLogger(CardStorageCsv.class);

    @Override
    public Flux<BankCard> importFile(File file) {
        return Flux.using(
                        () -> Files.newBufferedReader(file.toPath(), StandardCharsets.ISO_8859_1),
                        reader -> Flux.fromStream(reader.lines().skip(1)) // Skip header
                                .doOnNext(line -> logger.info("Processing line: " + line))
                                .map(line -> {
                                    String[] fields = line.split(",");
                                    BankCard card = new BankCard(
                                            fields[0],
                                            Long.parseLong(fields[1]),
                                            LocalDate.parse(fields[2])); // Use LocalDate.parse for expirationDate
                                    card.setCreatedAt(LocalDateTime.parse(fields[3]));
                                    card.setUpdatedAt(LocalDateTime.parse(fields[4]));
                                    return card;
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
    public Mono<Void> exportFile(File file, List<BankCard> cards) {
        return Mono.fromRunnable(() -> {
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.ISO_8859_1)) {
                logger.info("Iniciando la escritura del archivo CSV: {}", file.getPath());
                writer.write("number,clientId,expirationDate,createdAt,updatedAt");
                writer.newLine();
                for (BankCard card : cards) {
                    writer.write(String.format("%s,%d,%s,%s,%s",
                            card.getNumber(),
                            card.getClientId(),
                            card.getExpirationDate(),
                            card.getCreatedAt(),
                            card.getUpdatedAt()));
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