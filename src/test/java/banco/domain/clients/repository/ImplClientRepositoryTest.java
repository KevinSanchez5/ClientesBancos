package banco.domain.clients.repository;

import banco.data.local.LocalDatabaseManager;
import banco.domain.clients.model.Client;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ImplClientRepositoryTest {

    @Mock
    private LocalDatabaseManager localDatabaseManager;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private ImplClientRepository implClientRepository;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(localDatabaseManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void testFindAllClients() throws SQLException {
        // Simular los datos que regresará el ResultSet
        when(resultSet.next()).thenReturn(true).thenReturn(false); // Un cliente en la base de datos
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn("John Doe");
        when(resultSet.getString("username")).thenReturn("jdoe");
        when(resultSet.getString("email")).thenReturn("jdoe@example.com");
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        // Ejecutar el método findAll
        CompletableFuture<List<Client>> futureClients = implClientRepository.findAll();
        List<Client> clients = futureClients.join();

        // Verificar el resultado
        assertEquals(1, clients.size());
        assertEquals("John Doe", clients.get(0).getName());

        // Verificar que se llamó a la base de datos
        verify(preparedStatement, times(1)).executeQuery();
    }

    @Test
    void testFindById() throws SQLException {
        // Simular los datos para un cliente específico
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn("John Doe");
        when(resultSet.getString("username")).thenReturn("jdoe");
        when(resultSet.getString("email")).thenReturn("jdoe@example.com");
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        // Ejecutar el método findById
        Client futureClient = implClientRepository.findById(1L).join();


        // Verificar el resultado
        assertNotNull(futureClient);
        assertEquals("John Doe", futureClient.getName());

        // Verificar que se llamó a la base de datos
        verify(preparedStatement, times(1)).setLong(1, 1L);
        verify(preparedStatement, times(1)).executeQuery();
    }




}