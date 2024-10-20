package banco;

import banco.domain.clients.model.Client;
import banco.domain.clients.storage.json.ClientStorage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        importClients();
        exportClients();
    }

    public static void importClients() {
        ClientStorage reader = new ClientStorage();

        File file = null;
        try {
            file = Paths.get(ClassLoader.getSystemResource("example/clientes.json").toURI()).toFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Mono<Client> clientFlux = reader.importFile(file);

        clientFlux.subscribe(
                client -> System.out.println("Cliente recibido: " + client),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Lectura completa")
        );
    }



        public static void exportClients() {
        ClientStorage reader = new ClientStorage();
        Path path = Paths.get("src/main/resources/example/archivo_export.json");

        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
            return;
        }

        File file = path.toFile();
        List<Client> clients = new ArrayList<>();
        clients.add(new Client(1L, "John Doe", "johndoe", "john.doe@example.com"));
        clients.add(new Client(2L, "Jane Smith", "janesmith", "jane.smith@example.com"));

        for (Client client : clients) {
            client.setCards(new ArrayList<>());
            client.setCreatedAt(LocalDateTime.now());
            client.setUpdatedAt(LocalDateTime.now());
        }

        System.out.println("Exportando clientes: " + clients);

        reader.exportFile(file, clients)
                .doOnSubscribe(sub -> System.out.println("Iniciando exportación..."))
                .doOnSuccess(success -> System.out.println("Clientes exportados exitosamente"))
                .doOnError(error -> System.err.println("Error: " + error.getMessage()))
                .block();  // Cambia a .block() para asegurar ejecución síncrona
    }
}