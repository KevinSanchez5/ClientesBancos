package banco.domain.clients.service;

import banco.domain.cards.repository.BankCardRepository;
import banco.domain.clients.exceptions.ClientExceptionBadRequest;
import banco.domain.clients.exceptions.ClientNotFoundException;
import banco.domain.clients.model.Client;
import banco.domain.clients.model.notification.NotificationEvent;
import banco.domain.clients.model.notification.NotificationType;
import banco.domain.clients.repository.ClientRemoteRepository;
import banco.domain.clients.repository.ClientRepository;
import banco.domain.clients.service.cache.ClientesCacheImpl;
import banco.domain.clients.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.Null;
import org.testcontainers.shaded.org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImplClientServiceTest {

    @Mock
    private ClientRepository localClientRepository;

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private ClientRemoteRepository remoteClientRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ClientesCacheImpl cache;

    @InjectMocks
    private ImplClientService clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clientService = ImplClientService.getInstance(localClientRepository, bankCardRepository, remoteClientRepository, notificationService);
    }

    @Test
    void findAllClients_ShouldReturnClientsFromRemoteRepository() {
        // Arrange
        Client client1 = new Client(1L, "John Doe", "jdoe", "jdoe@example.com", null, null, null);
        Client client2 = new Client(2L, "Jane Doe", "jadoe", "jadoe@example.com", null, null, null);
        when(remoteClientRepository.getAll()).thenReturn(Arrays.asList(client1, client2));

        // Act
        List<Client> clients = clientService.findAllClients();

        // Assert
        assertEquals(2, clients.size());
        verify(remoteClientRepository, times(1)).getAll();
    }

    @Test
    void findByClientId_ShouldReturnClientFromLocal() {
        // Arrange
        Long clientId = 1L;
        Client client = new Client(clientId, "John Doe", "jdoe", "jdoe@example.com", null, null, null);
        when(localClientRepository.findById(clientId)).thenReturn(CompletableFuture.completedFuture(client));

        // Act
        Client result = clientService.findByClientId(clientId);

        // Assert
        assertEquals(client, result);
        verify(localClientRepository, times(1)).findById(clientId);
    }




    @Test
    void testSaveClientSuccess() throws ClientExceptionBadRequest {
        // Arrange
        Client client = new Client(1L, "John Doe", "jdoe", "jdoe@example.com", null, null, null);
        when(cache.get(client.getId())).thenReturn(null);
        when(localClientRepository.findById(client.getId())).thenReturn(CompletableFuture.completedFuture(null));
        when(remoteClientRepository.createClient(client)).thenReturn(client);
        when(localClientRepository.save(client)).thenReturn(CompletableFuture.completedFuture(client));

        // Act
        Client savedClient = clientService.saveClient(client);

        // Assert
        assertNotNull(savedClient);
        verify(localClientRepository, times(1)).save(client);


        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationService, times(1)).sendNotification(eventCaptor.capture());
        NotificationEvent capturedEvent = eventCaptor.getValue();
        assertEquals(NotificationType.CREATE, capturedEvent.getType());
        assertEquals(client, capturedEvent.getClient());
    }


    @Test
    void testSaveClientExistsInRepository() {
        // Arrange
        Client client = new Client(1L, "John Doe", "jdoe", "jdoe@example.com", null, null, null);
        when(cache.get(client.getId())).thenReturn(null);
        when(localClientRepository.findById(client.getId())).thenReturn(CompletableFuture.completedFuture(client));

        // Act & Assert
        ClientExceptionBadRequest thrown = assertThrows(ClientExceptionBadRequest.class, () -> clientService.saveClient(client));
        assertEquals("El cliente ya existe en el repositorio local", thrown.getMessage());

    }
        @Test
        void updateClient () {
        }

        @Test
        void deleteClient () {
        }

        @Test
        void findAllBankCards () {
        }

        @Test
        void findBankCardsByClientId () {
        }

        @Test
        void findBankCardByNumber () {
        }

        @Test
        void saveBankCard () {
        }

        @Test
        void updateBankCard () {
        }

        @Test
        void deleteBankCard () {
        }

        @Test
        void importClientsFromJsonFile () {
        }
    }

