package banco.domain.clients.services.clientes;

import banco.domain.clients.model.Client;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ClientesCacheImplTest {

    private final ClientesCache cache = new ClientesCacheImpl(10);

    @Test
    void put() {
        //Act
        Client client = new Client(1L, "John Doe", "1234567890",
                "hfhkjhsfsbf");
        cache.put(1L, client);

        //Assert
        assertEquals(client, cache.get(1L));
    }

    @Test
    void get() {
        //Arrange
        Client client = new Client(1L, "John Doe", "1234567890",
                "hfhkjhsfsbf");
        cache.put(1L, client);

        //Act
        Client fetchedClient = cache.get(1L);

        //Assert
        assertEquals(client, fetchedClient);
    }

    @Test
    void getReturnsNullWhenClientDoesntExist() {
        //Act
        Client fetchedClient = cache.get(4L);

        //Assert
        assertNull(fetchedClient);
    }

    @Test
    void remove() {
        //Arrange
        Client client = new Client(1L, "John Doe", "1234567890",
                "hfhkjhsfsbf");
        cache.put(1L, client);

        //Act
        cache.remove(1L);

        //Assert
        assertNull(cache.get(1L));
    }

    @Test
    void clear() throws InterruptedException {
        //Arrange
        Client client = new Client(1L, "John Doe", "1234567890",
                "hfhkjhsfsbf");

        //Act
        client.setUpdatedAt(LocalDateTime.now().minusMinutes(2));
        cache.put(1L, client);
        cache.clear(); // Lo pongo asi para no tener que esperar un minuto a que salte solo

        //Assert
        assertNull(cache.get(1L));
    }

    @Test
    void clearDoesntRemoveNonExpiredEntries() throws InterruptedException {
        //Arrange
        Client client = new Client(2L, "John Doe", "1234567890",
                "hfhkjhsfsbf");

        //Act
        client.setUpdatedAt(LocalDateTime.now());
        cache.put(2L, client);
        cache.clear(); // Lo pongo asi para no tener que esperar un minuto a que salte solo

        //Assert
        assertNotNull(cache.get(2L));
    }

    @Test
    void shutDownStopsTheCleanerFromRunning() throws InterruptedException {
        // Arrange
        //Arrange
        Client client = new Client(2L, "John Doe", "1234567890",
                "hfhkjhsfsbf");
        cache.put(10L, client);

        //Act
        cache.shutdown();
        TimeUnit.MINUTES.sleep(1);

        //Assert
        assertNotNull(cache.get(10L));
    }
}