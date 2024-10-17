package banco;

import banco.data.local.LocalDatabaseConfig;
import banco.data.local.LocalDatabaseInitializer;
import banco.data.local.LocalDatabaseManager;
import banco.domain.cards.model.BankCard;
import banco.domain.clients.model.Client;
import banco.domain.clients.repository.ImplClientRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        try {
            // Inicializar la base de datos
            LocalDatabaseConfig config = new LocalDatabaseConfig("src/main/resources/localclients/database.properties");
            LocalDatabaseManager localDatabaseManager = LocalDatabaseManager.getInstance();
            LocalDatabaseInitializer initializer = new LocalDatabaseInitializer(config, localDatabaseManager);
            initializer.initializeDatabase();

            initializer.listTables();


            ImplClientRepository clientRepository = ImplClientRepository.getInstance(localDatabaseManager);



            // Crear una tarjeta y un cliente
            BankCard card1 = new BankCard("1234567890123456", 1L, LocalDate.of(2025, 12, 31), null, null);
            ArrayList<BankCard> cards = new ArrayList<>();
            cards.add(card1);

            Client newClient = new Client(null, "Jane Doe", "janedoe", "jane.doe@example.com", cards, null, null);

            // Guardar el cliente y su tarjeta
            CompletableFuture<Void> future = clientRepository.save(newClient).thenAccept(savedClient -> {
                System.out.println("Cliente guardado: " + savedClient);

                // Buscar el cliente por su ID
                clientRepository.findById(savedClient.getId()).thenAccept(client -> {
                    System.out.println("Cliente encontrado: " + client);

                    // Actualizar al cliente
                    savedClient.setName("Jane Smith");
                    clientRepository.update(savedClient.getId(), savedClient).thenRun(() -> {
                        System.out.println("Cliente actualizado.");

                        // Volver a buscar el cliente actualizado
                        clientRepository.findById(savedClient.getId()).thenAccept(updatedClient -> {
                            System.out.println("Cliente actualizado encontrado: " + updatedClient);

                            // Eliminar el cliente
                            clientRepository.delete(savedClient.getId()).thenRun(() -> {
                                System.out.println("Cliente eliminado.");
                            });
                        });
                    });
                });
            });

            // Usar join para esperar a que todas las tareas asíncronas terminen
            future.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}