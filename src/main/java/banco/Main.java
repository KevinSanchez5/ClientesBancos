package banco;

import banco.data.locale.LocalDatabaseConfig;
import banco.data.locale.LocalDatabaseInitializer;
import banco.domain.clients.model.Client;
import banco.domain.clients.repository.ImplClientRepository;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        try {
            LocalDatabaseConfig config = new LocalDatabaseConfig("src/main/resources/database.properties");
            LocalDatabaseInitializer initializer = new LocalDatabaseInitializer(config);
            initializer.initializeDatabase();

            ImplClientRepository clientRepository = ImplClientRepository.getInstance();

            // guardar cliente
            Client newClient = new Client(null, "Jane Doe", "janedoe", "jane.doe@example.com", new ArrayList<>(), null, null);
            clientRepository.save(newClient).thenAccept(client -> {
                System.out.println("Cliente guardado: " + client);
            });

            Thread.sleep(1500);

            clientRepository.findById(1L).thenAccept(client -> {
                System.out.println("Cliente encontrado: " + client);
            });

            Thread.sleep(2000);


            Client updatedClient = new Client(1L, "Jane Smith", "janesmith", "jane.smith@example.com", new ArrayList<>(), null, null);
            clientRepository.update(1L, updatedClient).thenRun(() -> {
                System.out.println("Cliente actualizado.");
            });

            Thread.sleep(2000);

            clientRepository.findAll().thenAccept(clients -> {
                System.out.println("Clientes en la base de datos: ");
                clients.forEach(System.out::println);
            });

            clientRepository.delete(1L).thenRun(() -> {
                System.out.println("Cliente eliminado.");
            });

            Thread.sleep(2000);

            clientRepository.findAll().thenAccept(clients -> {
                System.out.println("Clientes en la base de datos: ");
                clients.forEach(System.out::println);
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}