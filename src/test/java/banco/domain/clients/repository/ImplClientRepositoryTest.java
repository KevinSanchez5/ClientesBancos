package banco.domain.clients.repository;

import banco.data.local.LocalDatabaseConfig;
import banco.data.local.LocalDatabaseInitializer;
import banco.data.local.LocalDatabaseManager;
import banco.domain.cards.model.BankCard;
import banco.domain.clients.model.Client;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class ImplClientRepositoryTest {

    private static ImplClientRepository repository;

    @BeforeAll
    public static void setUp() throws IOException {
        LocalDatabaseConfig config = new LocalDatabaseConfig("localclients/database.properties");
        LocalDatabaseManager db = LocalDatabaseManager.getInstance();
        LocalDatabaseInitializer initializer = new LocalDatabaseInitializer(config, db);
        initializer.initializeDatabase();
        repository = ImplClientRepository.getInstance(db);
    }

    @Test
    void save() throws ExecutionException, InterruptedException {
        //Arrange
        Client client = Client.builder()
                .name("Juan")
                .username("test")
                .email("example@example.com")
                .cards(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();

        //Act
        Client savedClient = repository.save(client).get();

        //Assert
        assertNotNull(savedClient);
        assertAll(
                () -> assertEquals(client.getName(), savedClient.getName()),
                () -> assertEquals(client.getUsername(), savedClient.getUsername()),
                () -> assertEquals(client.getEmail(), savedClient.getEmail()),
                ()-> assertNotNull(savedClient.getCreatedAt()),
                ()-> assertNotNull(savedClient.getUpdatedAt())
        );

        //Clean up
        repository.delete(savedClient.getId()).get();
    }



    @Test
    void findById() throws ExecutionException, InterruptedException {
        //Arrange
        Client client = Client.builder()
                .name("Juan")
                .username("test")
                .email("example@example.com")
                .cards(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        //Act
        Client savedClient = repository.save(client).get();
        Client foundClient = repository.findById(savedClient.getId()).get();

        //Assert
        assertNotNull(foundClient);
        assertAll(
                () -> assertEquals(client.getName(), foundClient.getName()),
                () -> assertEquals(client.getUsername(), foundClient.getUsername()),
                () -> assertEquals(client.getEmail(), foundClient.getEmail()),
                ()-> assertNotNull(foundClient.getCreatedAt()),
                ()-> assertNotNull(foundClient.getUpdatedAt())
        );

        //Clean up
        repository.delete(savedClient.getId()).get();
    }


    @Test
    void findAll() throws ExecutionException, InterruptedException {
        //Act
        Client client = Client.builder()
               .name("Juan")
               .username("test")
               .email("example@example.com")
               .cards(Collections.emptyList())
               .createdAt(LocalDateTime.now())
               .updatedAt(LocalDateTime.now())
               .build();

        repository.save(client).get();
        List<Client> clients = repository.findAll().get();

        //Assert
        assertNotNull(clients);
        assertEquals(1, clients.size());

        //Clean up
        repository.delete(client.getId()).get();
    }

    @Test
    void update() throws ExecutionException, InterruptedException {
        //Arrange
        Client client = Client.builder()
                .name("Juan")
                .username("test")
                .email("example@example.com")
                .cards(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Client updatedClient = Client.builder()
                .name("Juana")
                .username("test2")
                .email("noexample@example.com")
                .cards(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Client savedClient = repository.save(client).get();

        //Act
        Client result = repository.update(savedClient.getId(), updatedClient).get();

        //Assert
        assertNotNull(result);
        assertAll(
                () -> assertEquals(updatedClient.getName(), result.getName()),
                () -> assertEquals(updatedClient.getUsername(), result.getUsername()),
                () -> assertEquals(updatedClient.getEmail(), result.getEmail()),
                ()-> assertNotNull(result.getCreatedAt()),
                ()-> assertNotNull(result.getUpdatedAt())
        );

        //Clean up
        repository.delete(savedClient.getId()).get();
    }

    @Test
    void delete() throws ExecutionException, InterruptedException {
        //Arrange
        Client client = Client.builder()
               .name("Juan")
               .username("test")
               .email("example@example.com")
               .cards(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Client savedClient = repository.save(client).get();

        //Act
        repository.delete(savedClient.getId()).get();

        //Assert
        Client deletedClient = repository.findById(savedClient.getId()).get();
        assertNull(deletedClient);
    }

    @Test
    void findAllCardsByClientId() throws ExecutionException, InterruptedException {
        //Arrange
        Client client = Client.builder()
               .name("Juan")
               .username("test")
               .email("example@example.com")
               .cards(Collections.emptyList())
               .createdAt(LocalDateTime.now())
               .updatedAt(LocalDateTime.now())
               .build();

        Client savedClient = repository.save(client).get();

        //Act
        List<Client> clients = repository.findAll().get();

        //Assert
        assertNotNull(clients);
        assertEquals(1, clients.size());

        //Clean up
        repository.delete(savedClient.getId()).get();
    }

    @Test
    void saveBankCard() throws ExecutionException, InterruptedException {
        //Arrange
        BankCard card = BankCard.builder()
                .number("123")
                .clientId(1L)
                .expirationDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        //Act
        BankCard result = repository.saveBankCard(card).get();

        //Assert
        assertNotNull(result);
        assertEquals(card.getNumber(), result.getNumber());
        assertEquals(card.getClientId(), result.getClientId());
        assertEquals(card.getExpirationDate(), result.getExpirationDate());
        assertEquals(card.getCreatedAt(), result.getCreatedAt());
        assertEquals(card.getUpdatedAt(), result.getUpdatedAt());

        //Clean up
        repository.deleteBankCard(result.getNumber()).get();
    }

    @Test
    void updateBankCard() throws ExecutionException, InterruptedException {
        //Arrange
        BankCard card = BankCard.builder()
                .number("123")
                .clientId(1L)
                .expirationDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        BankCard updatedCard = BankCard.builder()
                .number("546")
                .clientId(1L)
                .expirationDate(LocalDate.now().plusDays(1))
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        //Act
        repository.saveBankCard(card).get();
        repository.updateBankCard("123", updatedCard).get();

        //Assert
        BankCard result = repository.findAllCardsByClientId(1L).get(0);
        assertEquals(updatedCard.getClientId(), result.getClientId());
        assertEquals(updatedCard.getExpirationDate(), result.getExpirationDate());

        //Clean up
        repository.deleteBankCard("546").get();
    }

    @Test
    void deleteBankCard() throws ExecutionException, InterruptedException {
        //Arrange
        BankCard card = BankCard.builder()
               .number("123")
               .clientId(1L)
               .expirationDate(LocalDate.now())
               .createdAt(LocalDateTime.now())
               .updatedAt(LocalDateTime.now())
               .build();

        //Act
        repository.saveBankCard(card).get();
        repository.deleteBankCard("123").get();

        //Assert
        BankCard result;
        try {
            result = repository.findAllCardsByClientId(1L).getFirst();
        }catch (NoSuchElementException e){
            result = null;
        }
        assertNull(result);
    }
}