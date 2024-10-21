package banco;

import banco.domain.clients.model.Client;
import banco.domain.clients.storage.csv.ClientStorageCsv;
import banco.domain.clients.storage.json.ClientStorageJson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        //importClientFromCSV();
       //exportClientsToCSV();
        importClientFromJSON();
        exportClientsToJSON();
    }

    public static void exportClientsToCSV() {
        ClientStorageCsv storage = new ClientStorageCsv();
        Path path = Paths.get("src/main/resources/example/archivo_export.csv");

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

        storage.exportFile(file, clients)
                .doOnSubscribe(sub -> System.out.println("Iniciando exportación..."))
                .doOnSuccess(success -> System.out.println("Clientes exportados exitosamente"))
                .doOnError(error -> System.err.println("Error: " + error.getMessage()))
                .block();  // Forzar ejecución síncrona
    }

    public static void importClientFromCSV() {
        ClientStorageCsv storage = new ClientStorageCsv();
        Path path = Paths.get("src/main/resources/example/ejemplo.csv");

        File file = path.toFile();

        System.out.println("Checking file: " + file.getAbsolutePath());

        if (!file.exists()) {
            System.err.println("El archivo no existe: " + file.getPath());
            return;
        }

        storage.importFile(file)
                .doOnSubscribe(sub -> System.out.println("Iniciando importación..."))
                .doOnNext(client -> System.out.println("Cliente recibido: " + client))
                .doOnError(error -> System.err.println("Error: " + error.getMessage()))
                .doOnTerminate(() -> System.out.println("Lectura completa"))
                .blockLast();  // Forzar ejecución síncrona
    }


    public static void exportClientsToJSON() {
        ClientStorageJson storage = new ClientStorageJson();
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

        storage.exportFile(file, clients)
                .doOnSubscribe(sub -> System.out.println("Iniciando exportación..."))
                .doOnSuccess(success -> System.out.println("Clientes exportados exitosamente"))
                .doOnError(error -> System.err.println("Error: " + error.getMessage()))
                .block();  // Forzar ejecución síncrona
    }

    public static void importClientFromJSON() {
        ClientStorageJson storage = new ClientStorageJson();
        Path path = Paths.get("src/main/resources/example/clients.json");

        File file = path.toFile();

        System.out.println("Checking file: " + file.getAbsolutePath());

        if (!file.exists()) {
            System.err.println("El archivo no existe: " + file.getPath());
            return;
        }

        storage.importFile(file)
                .doOnSubscribe(sub -> System.out.println("Iniciando importación..."))
                .doOnNext(client -> System.out.println("Cliente recibido: " + client))
                .doOnError(error -> System.err.println("Error: " + error.getMessage()))
                .doOnTerminate(() -> System.out.println("Lectura completa"))
                .blockLast();  // Forzar ejecución síncrona hasta el último elemento
    }
}

