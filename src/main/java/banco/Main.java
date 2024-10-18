package banco;

import banco.data.locale.LocalDatabaseConfig;
import banco.data.locale.LocalDatabaseInitializer;
import banco.domain.cards.database.RemoteDatabaseManager;
import banco.domain.cards.model.BankCard;
import banco.domain.cards.repository.BankCardRepositoryImpl;
import banco.domain.clients.model.Client;
import banco.domain.clients.repository.ImplClientRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        try {
            LocalDatabaseConfig config = new LocalDatabaseConfig("database.properties");
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

            RemoteDatabaseManager db= RemoteDatabaseManager.getInstance();
            BankCardRepositoryImpl bankCardRepository = BankCardRepositoryImpl.getInstance(db);

            bankCardRepository.save(
                    BankCard.builder()
                            .number("9876543210987654")
                            .clientId(1L)
                            .expirationDate(LocalDate.now().plusYears(3))
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            );

            System.out.println(bankCardRepository.findById("9876543210987654").get().toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}