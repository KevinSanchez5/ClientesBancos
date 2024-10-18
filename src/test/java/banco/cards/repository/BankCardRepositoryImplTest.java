package banco.cards.repository;

import banco.domain.cards.model.BankCard;
import banco.domain.cards.database.RemoteDatabaseManager;
import banco.domain.cards.repository.BankCardRepositoryImpl;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BankCardRepositoryImplTest {

    private static BankCardRepositoryImpl repository;

    @Container
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.0-alpine")
            .withDatabaseName("test")
            .withPassword("password")
            .withUsername("postgres")
            .withInitScript("bankcards/init.sql")
            .withExposedPorts(5432)
            .withNetworkMode("bridge")
            .waitingFor(Wait.forListeningPort());

    @BeforeAll
    public static void setUp(){
        postgres.start();
        RemoteDatabaseManager db = RemoteDatabaseManager.getTestInstance(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
        repository = BankCardRepositoryImpl.getInstance(db);
    }

    @AfterAll
    public static void tearDown() {
        if (postgres != null) {
            postgres.stop(); // Paramos el contenedor
        }
    }

    @Test
    @Order(1)
    void testSave() throws ExecutionException, InterruptedException {
        //Arrange
        BankCard bankCard = BankCard.builder()
                .number("1234567812345678")
                .clientId(1L)
                .expirationDate(LocalDate.now().plusYears(3))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        //Act
        BankCard savedCard = repository.save(bankCard).get();

        //Assert
        assertNotNull(savedCard);
        assertEquals("1234567812345678", savedCard.getNumber());
    }

    @Test
    @Order(2)
    void testFindById() throws ExecutionException, InterruptedException {
        //Arrange
        BankCard bankCard = BankCard.builder()
                .number("9876543210987654")
                .clientId(1L)
                .expirationDate(LocalDate.now().plusYears(3))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        //Act
        repository.save(bankCard).get();
        BankCard foundCard = repository.findById("9876543210987654").get();

        //Assert
        assertNotNull(foundCard);
        assertEquals("9876543210987654", foundCard.getNumber());
    }

    @Test
    @Order(3)
    void testFindAll() throws ExecutionException, InterruptedException {
        //Act
        List<BankCard> cards = repository.findAll().get();

        //Assert
        assertNotNull(cards);
        assertFalse(cards.isEmpty());
    }

    @Test
    @Order(4)
    void testUpdate() throws ExecutionException, InterruptedException {
        //Arrange
        BankCard bankCard = BankCard.builder()
                .number("1111222233334444")
                .clientId(1L)
                .expirationDate(LocalDate.now().plusYears(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        repository.save(bankCard).get();

        BankCard updatedCard = BankCard.builder()
                .number("1111222233334444")
                .expirationDate(LocalDate.now().plusYears(4))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        //Act
        BankCard result = repository.update("1111222233334444" , updatedCard).get();

        //Assert
        assertNotNull(result);
        assertEquals("1111222233334444", result.getNumber());
    }

    @Test
    @Order(5)
    void testDelete() throws ExecutionException, InterruptedException {
        //Arrange
        UUID uuid = UUID.randomUUID();
        BankCard bankCard = BankCard.builder()
                .number("9999000011112222")
                .clientId(1L)
                .expirationDate(LocalDate.now().plusYears(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        repository.save(bankCard).get();

        //Act
        boolean deleted = repository.delete("9999000011112222").get();

        //Assert
        assertTrue(deleted);
        BankCard deletedCard = repository.findById("9999000011112222").get();
        assertNull(deletedCard);  // It should return null if deleted successfully
    }

    @Test
    @Order(6)
    void testGetBankCardsByClientId() throws ExecutionException, InterruptedException {
        // Arrange

        BankCard card1 = BankCard.builder()
                .number("1122334455667788")
                .clientId(9L)
                .expirationDate(LocalDate.now().plusYears(3))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        BankCard card2 = BankCard.builder()
                .number("2233445566778899")
                .clientId(9L)
                .expirationDate(LocalDate.now().plusYears(3))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        repository.save(card1).get();
        repository.save(card2).get();

        // Act
        List<BankCard> cards = repository.getBankCardsByClientId(9L).get();

        // Assert
        assertNotNull(cards);
        assertEquals(2, cards.size());
    }

    @Test
    @Order(7)
    void testSaveDuplicateBankCard() throws ExecutionException, InterruptedException {
        // Arrange
        BankCard bankCard1 = BankCard.builder()
                .number("1234567812385678")
                .clientId(1L)
                .expirationDate(LocalDate.now().plusYears(3))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        BankCard bankCard2 = BankCard.builder()
                .number("1234567812385678")
                .clientId(1L)
                .expirationDate(LocalDate.now().plusYears(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act & Assert
        repository.save(bankCard1).get();
        Exception exception = assertThrows(ExecutionException.class, () -> {
            repository.save(bankCard2).get();
        });

        // Assert
        String expectedMessage = "ERROR: duplicate key value violates unique constraint \"bankcards_pkey\"\n" +
                "  Detail: Key (number)=(" + "1234567812385678" + ") already exists."; // Adjust this based on your exception handling
        String actualMessage = exception.getCause().getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @Order(8)
    void testFindNonExistentBankCard() throws ExecutionException, InterruptedException {
        // Arrange
        String nonExistentNumber = "1234567812345697";

        // Act
        BankCard foundCard = repository.findById(nonExistentNumber).get();

        // Assert
        assertNull(foundCard, "Expected null for non-existent bank card");
    }

    @Test
    @Order(9)
    void testDeleteNonExistentBankCard() throws ExecutionException, InterruptedException {
        // Arrange
        String nonExistentNumber = "1234567812345697";

        // Act
        boolean deleted = repository.delete(nonExistentNumber).get();

        // Assert
        assertFalse(deleted, "Expected delete to return false for non-existent bank card");
    }

    @Test
    @Order(10)
    void testUpdateNonExistentBankCard(){
        // Arrange
        String nonExistentNumber = "1234567812345697";
        BankCard updatedCard = BankCard.builder()
                .number("5555666677778888")
                .expirationDate(LocalDate.now().plusYears(4))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act & Assert
        Exception exception = assertThrows(ExecutionException.class, () -> {
            repository.update(nonExistentNumber, updatedCard).get();
        });
        String expectedMessage = "Tarjeta de crédito no encontrada con id: " + nonExistentNumber ;
        String actualMessage = exception.getCause().getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @Order(11)
    void testGetBankCardsByNonExistentClientId() throws ExecutionException, InterruptedException {
        // Arrange
        Long nonExistentClient = 10L;

        // Act
        List<BankCard> cards = repository.getBankCardsByClientId(nonExistentClient).get();

        // Assert
        assertNotNull(cards, "Expected empty list for non-existent client ID");
        assertTrue(cards.isEmpty(), "Expected empty list for non-existent client ID");
    }

}
