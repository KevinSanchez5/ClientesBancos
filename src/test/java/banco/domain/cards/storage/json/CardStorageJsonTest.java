package banco.domain.cards.storage.json;

import banco.domain.cards.model.BankCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

class CardStorageJsonTest {

    private CardStorageJson storage;
    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        storage = new CardStorageJson();
        testFile = Files.createTempFile("test_bankcards", ".json").toFile();
    }

    @Test
    void testExportFile() {
        List<BankCard> cards = Arrays.asList(
                new BankCard("1234567890123456", 1L, LocalDate.of(2025, 1, 1)),
                new BankCard("9876543210987654", 2L, LocalDate.of(2026, 1, 1))
        );
        cards.forEach(card -> {
            card.setCreatedAt(LocalDateTime.now());
            card.setUpdatedAt(LocalDateTime.now());
        });

        Mono<Void> result = storage.exportFile(testFile, cards);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testImportFile() throws IOException {
        String json = "[{\"number\":\"1234567890123456\",\"clientId\":1,\"expirationDate\":\"2025-01-01\",\"createdAt\":\"2023-04-05T10:15:30\",\"updatedAt\":\"2023-04-05T10:15:30\"},{\"number\":\"9876543210987654\",\"clientId\":2,\"expirationDate\":\"2026-01-01\",\"createdAt\":\"2023-04-05T10:15:30\",\"updatedAt\":\"2023-04-05T10:15:30\"}]";
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(json);
        }

        Flux<BankCard> result = storage.importFile(testFile);

        StepVerifier.create(result)
                .expectNextMatches(card -> card.getNumber().equals("1234567890123456"))
                .expectNextMatches(card -> card.getNumber().equals("9876543210987654"))
                .verifyComplete();
    }

    @Test
    void testImportFileMultipleCards() throws IOException {
        String json = "[{\"number\":\"1234567890123456\",\"clientId\":1,\"expirationDate\":\"2025-01-01\",\"createdAt\":\"2023-04-05T10:15:30\",\"updatedAt\":\"2023-04-05T10:15:30\"},{\"number\":\"9876543210987654\",\"clientId\":2,\"expirationDate\":\"2026-01-01\",\"createdAt\":\"2023-04-05T10:15:30\",\"updatedAt\":\"2023-04-05T10:15:30\"}]";
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(json);
        }

        Mono<List<BankCard>> result = storage.importFileMultipleCards(testFile);

        StepVerifier.create(result)
                .expectNextMatches(cards ->
                        cards.size() == 2 &&
                                cards.get(0).getNumber().equals("1234567890123456") &&
                                cards.get(1).getNumber().equals("9876543210987654"))
                .verifyComplete();
    }

    @Test
    void testExportFileWithError() {
        List<BankCard> cards = Arrays.asList(
                new BankCard("1234567890123456", 1L, LocalDate.of(2025, 1, 1)),
                new BankCard("9876543210987654", 2L, LocalDate.of(2026, 1, 1))
        );
        cards.forEach(card -> {
            card.setCreatedAt(LocalDateTime.now());
            card.setUpdatedAt(LocalDateTime.now());
        });

        File nonWritableFile = new File("/non_writable_directory/test_bankcards.json");

        Mono<Void> result = storage.exportFile(nonWritableFile, cards);

        StepVerifier.create(result)
                .expectError(UncheckedIOException.class)
                .verify();
    }

    @Test
    void testImportFileWithError() {
        File nonExistingFile = new File("non_existing_file.json");

        Flux<BankCard> result = storage.importFile(nonExistingFile);

        StepVerifier.create(result)
                .verifyComplete();
    }
}