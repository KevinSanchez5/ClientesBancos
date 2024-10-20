package banco.domain.clients.repository;

import banco.data.local.LocalDatabaseManager;
import banco.domain.clients.model.Client;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ImplClientRepositoryTest {

    private static ImplClientRepository repository;

    @Container
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.0-alpine")
            .withDatabaseName("test")
            .withUsername("testuser")
            .withPassword("password")
            .withInitScript("localclients/data.sql")
            .withExposedPorts(5432)
            .waitingFor(Wait.forListeningPort());

    @BeforeAll
    public static void setUp(){
        postgres.start();
        LocalDatabaseManager db = LocalDatabaseManager.getInstance();
        repository = ImplClientRepository.getInstance(db);
    }

    @AfterAll
    public static void tearDown() {
        if (postgres != null) {
            postgres.stop(); // Paramos el contenedor
        }
    }


    @Test
    @Order(1)
    void save() throws ExecutionException, InterruptedException {
        //Arrange
        Client client = Client.builder()
                .name("Juan")
                .username("test")
                .email("example@example.com")
                .cards(null).build();

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
    }



    @Test
    @Order(2)
    void findById() throws ExecutionException, InterruptedException {
        //Arrange
        Client client = Client.builder()
                .name("Juan")
                .username("test")
                .email("example@example.com")
                .cards(null).build();

        //Act
        Client savedClient = repository.save(client).resultNow();
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
    }


    @Test
    @Order(3)
    void findAll() throws ExecutionException, InterruptedException {
        //Act
        List<Client> clients = repository.findAll().get();

        //Assert
        assertNotNull(clients);
        assertFalse(clients.iterator().hasNext());
        assertEquals(1, clients.size());
    }

    @Test
    @Order(4)
    void update() throws ExecutionException, InterruptedException {
        //Arrange
        Client client = Client.builder()
                .name("Juan")
                .username("test")
                .email("example@example.com")
                .cards(null).build();

        Client updatedClient = Client.builder()
                .name("Juana")
                .username("test2")
                .email("noexample@example.com")
                .cards(null).build();

        Client savedClient = repository.save(client).resultNow();

        //Act
        Client result = repository.update(savedClient.getId(), updatedClient).get();


    }

    @Test
    void delete() throws ExecutionException, InterruptedException {
        //Arrange
        Client client = Client.builder()
               .name("Juan")
               .username("test")
               .email("example@example.com")
               .cards(null).build();

        Client savedClient = repository.save(client).get();

        //Act
        repository.delete(savedClient.getId()).get();

        //Assert
        Client deletedClient = repository.findById(savedClient.getId()).get();
        assertNull(deletedClient);
    }

    @Test
    void findAllCardsByClientId() {
    }

    @Test
    void saveBankCard() {
    }

    @Test
    void updateBankCard() {
    }

    @Test
    void deleteBankCard() {
    }
}