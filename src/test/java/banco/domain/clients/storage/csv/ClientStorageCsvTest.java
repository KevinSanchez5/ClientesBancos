package banco.domain.clients.storage.csv;

import banco.domain.clients.model.Client;
import banco.domain.clients.storage.csv.ClientStorageCsv;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

class ClientStorageCsvTest {

    private ClientStorageCsv storage;
    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        storage = new ClientStorageCsv();
        testFile = Files.createTempFile("test_clients", ".csv").toFile();
    }

    @Test
    void testExportFile() {
        List<Client> clients = Arrays.asList(
                new Client(1L, "John Doe", "johndoe", "john.doe@example.com"),
                new Client(2L, "Jane Smith", "janesmith", "jane.smith@example.com")
        );
        clients.forEach(client -> {
            client.setCreatedAt(LocalDateTime.now());
            client.setUpdatedAt(LocalDateTime.now());
        });

        Mono<Void> result = storage.exportFile(testFile, clients);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testImportFile() throws IOException {
        String csv = "id,name,username,email,createdAt,updatedAt\n" +
                "1,John Doe,johndoe,john.doe@example.com,2023-04-05T10:15:30,2023-04-05T10:15:30\n" +
                "2,Jane Smith,janesmith,jane.smith@example.com,2023-04-05T10:15:30,2023-04-05T10:15:30";
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(csv);
        }

        Flux<Client> result = storage.importFile(testFile);

        StepVerifier.create(result)
                .expectNextMatches(client -> client.getName().equals("John Doe"))
                .expectNextMatches(client -> client.getName().equals("Jane Smith"))
                .verifyComplete();
    }

    @Test
    void testExportFileWithError() {
        List<Client> clients = Arrays.asList(
                new Client(1L, "John Doe", "johndoe", "john.doe@example.com"),
                new Client(2L, "Jane Smith", "janesmith", "jane.smith@example.com")
        );
        clients.forEach(client -> {
            client.setCreatedAt(LocalDateTime.now());
            client.setUpdatedAt(LocalDateTime.now());
        });

        File nonWritableFile = new File("/non_writable_directory/test_clients.csv");

        Mono<Void> result = storage.exportFile(nonWritableFile, clients);

        StepVerifier.create(result)
                .expectError(UncheckedIOException.class)
                .verify();
    }

    @Test
    void testImportFileWithError() {
        File nonExistingFile = new File("non_existing_file.csv");

        Flux<Client> result = storage.importFile(nonExistingFile);

        StepVerifier.create(result)
                .expectError(IOException.class) // Cambiamos la expectativa para capturar IOException
                .verify();
    }

}
