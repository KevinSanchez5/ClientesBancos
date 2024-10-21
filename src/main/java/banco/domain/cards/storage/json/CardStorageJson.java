package banco.domain.cards.storage.json;

import banco.domain.cards.model.BankCard;
import banco.util.Storage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
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

public class CardStorageJson implements Storage<BankCard> {

    private final Logger logger = LoggerFactory.getLogger(CardStorageJson.class);
    private final ObjectMapper objectMapper;

    public CardStorageJson() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Flux<BankCard> importFile(File file) {
        return Flux.defer(() -> {
            try (InputStream inputStream = new FileInputStream(file);
                 Reader reader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1)) {
                BankCard[] bankCards = objectMapper.readValue(reader, BankCard[].class);
                return Flux.fromArray(bankCards);
            } catch (IOException e) {
                logger.error("Error reading JSON file: {}", e.getMessage());
                return Flux.empty();
            }
        });
    }

    @Override
    public Mono<Void> exportFile(File file, List<BankCard> bankCards) {
        return Mono.fromRunnable(() -> {
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.ISO_8859_1)) {
                logger.info("Iniciando la escritura del archivo: {}", file.getPath());
                writer.write(objectMapper.writeValueAsString(bankCards));
                writer.newLine();
                logger.info("Finalizada la escritura del archivo: {}", file.getPath());
            } catch (IOException e) {
                logger.error("Error writing JSON file: {}", e.getMessage());
                throw new UncheckedIOException(e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<List<BankCard>> importFileMultipleCards(File file) {
        return Mono.<List<BankCard>>fromCallable(() -> {
                    try (InputStream inputStream = new FileInputStream(file);
                         Reader reader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1)) {
                        TypeFactory typeFactory = objectMapper.getTypeFactory();
                        return objectMapper.readValue(reader, typeFactory.constructCollectionType(List.class, BankCard.class));
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    logger.error("Error reading JSON file: {}", e.getMessage());
                    return Mono.empty();
                });
    }
}