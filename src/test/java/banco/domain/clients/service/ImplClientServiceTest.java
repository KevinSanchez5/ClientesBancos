package banco.domain.clients.service;

import banco.domain.cards.exceptions.BankCardNotFoundException;
import banco.domain.cards.model.BankCard;
import banco.domain.cards.repository.BankCardRepository;
import banco.domain.clients.exceptions.ClientExceptionBadRequest;
import banco.domain.clients.exceptions.ClientNotFound;
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

import java.time.LocalDate;
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
    void testUpdateClientSuccess() throws Exception {
        // Arrange
        Long clientId = 1L;
        Client client = new Client(clientId, "John", "Doe", "example@example.com", null, null, null);
        Client updatedClient = new Client(clientId, "John", "Doe Updated", "example@example.com", null, null, null);

        // Mocking validators, repositories and cache
        when(remoteClientRepository.updateClient(client)).thenReturn(updatedClient);
        when(localClientRepository.save(updatedClient)).thenReturn(CompletableFuture.completedFuture(updatedClient));
        doNothing().when(notificationService).sendNotification(any(NotificationEvent.class));

        // Act
        Client result = clientService.updateClient(clientId, client);

        // Assert
        assertEquals(updatedClient, result);

        // Verify that remote repository, local repository, and cache were called
        verify(remoteClientRepository, times(1)).updateClient(client);
        verify(localClientRepository, times(1)).save(updatedClient);

        // Verify that a notification was sent
        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationService, times(1)).sendNotification(captor.capture());
        NotificationEvent event = captor.getValue();
        assertEquals(NotificationType.UPDATE, event.getType());
        assertEquals(updatedClient, event.getClient());
    }

    @Test
    void testDeleteClientSuccess() throws ClientNotFound, ClientNotFoundException {
        // Arrange
        Long clientId = 1L;
        when(localClientRepository.delete(clientId)).thenReturn(CompletableFuture.completedFuture(null));
        doNothing().when(cache).remove(clientId);

        // Act
        clientService.deleteClient(clientId);

        // Assert
        verify(remoteClientRepository, times(1)).deleteClient(clientId.intValue());
        verify(localClientRepository, times(1)).delete(clientId);

        // Verify that notification is sent
        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationService, times(1)).sendNotification(captor.capture());
        NotificationEvent event = captor.getValue();
        assertEquals(NotificationType.DELETE, event.getType());
        assertNull(event.getClient());
    }

    @Test
    void testDeleteClientThrowsClientNotFound() throws ClientNotFoundException {
        // Arrange
        Long clientId = 1L;
        doThrow(new ClientNotFound(clientId.toString())).when(remoteClientRepository).deleteClient(clientId.intValue());

        // Act & Assert
        ClientNotFound exception = assertThrows(ClientNotFound.class, () -> {
            clientService.deleteClient(clientId);
        });

        assertEquals("Cliente con id " + clientId + " no encontrado", exception.getMessage());

        // Verify that no further operations are performed after the exception
        verify(localClientRepository, times(0)).delete(anyLong());
        verify(cache, times(0)).remove(anyLong());
        verify(notificationService, times(0)).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void testFindAllBankCardsSuccess() {
        // Arrange
        List<BankCard> bankCards = List.of(new BankCard("1234-5678-9012-3456", 1L, LocalDate.of(2200, 10, 20), null, null));
        when(bankCardRepository.findAll()).thenReturn(CompletableFuture.completedFuture(bankCards));

        // Act
        List<BankCard> result = clientService.findAllBankCards();

        // Assert
        assertEquals(bankCards, result);
        verify(bankCardRepository, times(1)).findAll();
    }

    @Test
    void testFindBankCardsByClientIdSuccess() {
        // Arrange
        Long clientId = 1L;
        List<BankCard> bankCards = List.of(new BankCard("1234-5678-9012-3456", clientId, LocalDate.of(2200, 10, 20), null, null));
        when(bankCardRepository.getBankCardsByClientId(clientId)).thenReturn(CompletableFuture.completedFuture(bankCards));

        // Act
        List<BankCard> result = clientService.findBankCardsByClientId(clientId);

        // Assert
        assertEquals(bankCards, result);
        verify(bankCardRepository, times(1)).getBankCardsByClientId(clientId);
    }

    @Test
    void testFindBankCardsByClientIdThrowsException() {
        // Arrange
        Long clientId = 1L;

        when(bankCardRepository.getBankCardsByClientId(clientId)).thenThrow(new RuntimeException("Error al obtener tarjetas"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
           clientService.findBankCardsByClientId(clientId);
        });

        assertEquals("Error al obtener tarjetas", exception.getMessage());
        verify(bankCardRepository, times(1)).getBankCardsByClientId(clientId);
    }

    @Test
    void testFindBankCardByNumberSuccess() {
        // Arrange
        String cardNumber = "1234-5678-9012-3456";
        BankCard expectedBankCard = new BankCard(cardNumber, 1L, LocalDate.of(2200, 10, 20), null, null);

        when(bankCardRepository.findById(cardNumber)).thenReturn(CompletableFuture.completedFuture(expectedBankCard));

        // Act
        BankCard result = clientService.findBankCardByNumber(cardNumber);

        // Assert
        assertEquals(expectedBankCard, result);
        verify(bankCardRepository, times(1)).findById(cardNumber);
    }
    @Test
    void testFindBankCardByNumberThrowsException() {
        // Arrange
        String cardNumber = "1234-5678-9012-3456";
        when(bankCardRepository.findById(cardNumber)).thenThrow(new RuntimeException("Error al buscar tarjeta"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientService.findBankCardByNumber(cardNumber);
        });

        assertEquals("Error al buscar tarjeta", exception.getMessage());
        verify(bankCardRepository, times(1)).findById(cardNumber);
    }

    @Test
    void testSaveBankCardSuccess() {
        // Arrange
        BankCard bankCard = new BankCard("1234-5678-9012-3456", 1L, LocalDate.of(2200, 10, 20), null, null);

        // Mock validation and repository saving

        when(localClientRepository.saveBankCard(bankCard)).thenReturn(CompletableFuture.completedFuture(null));
        when(bankCardRepository.save(bankCard)).thenReturn(CompletableFuture.completedFuture(bankCard));

        // Act
        BankCard result = clientService.saveBankCard(bankCard);

        // Assert
        assertEquals(bankCard, result);
        verify(localClientRepository, times(1)).saveBankCard(bankCard);
        verify(bankCardRepository, times(1)).save(bankCard);
    }



    @Test
    void testSaveBankCardThrowsExceptionDuringSave() {
        // Arrange
        BankCard bankCard = new BankCard("1234-5678-9012-3456", 1L, LocalDate.of(2200, 10, 20), null, null);

        // Mock repository save failure
        when(localClientRepository.saveBankCard(bankCard)).thenReturn(CompletableFuture.completedFuture(null));
        when(bankCardRepository.save(bankCard)).thenThrow(new RuntimeException("Error al guardar tarjeta"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientService.saveBankCard(bankCard);
        });
        verify(localClientRepository, times(1)).saveBankCard(bankCard);
        verify(bankCardRepository, times(1)).save(bankCard);
    }

    @Test
    void testUpdateBankCardSuccess() throws BankCardNotFoundException {
        // Arrange
        String cardNumber = "1234-5678-9012-3456";
        BankCard bankCard = new BankCard(cardNumber, 1L, LocalDate.of(2200, 10, 20), null, null);
        when(localClientRepository.updateBankCard(cardNumber, bankCard)).thenReturn(CompletableFuture.completedFuture(null));
        when(bankCardRepository.update(cardNumber, bankCard)).thenReturn(CompletableFuture.completedFuture(bankCard));

        // Act
        BankCard result = clientService.updateBankCard(cardNumber, bankCard);

        // Assert
        assertEquals(bankCard, result);
        verify(localClientRepository, times(1)).updateBankCard(cardNumber, bankCard);
        verify(bankCardRepository, times(1)).update(cardNumber, bankCard);
    }



    @Test
    void testUpdateBankCardThrowsExceptionDuringUpdate() {
        // Arrange
        String cardNumber = "1234-5678-9012-3456";
        BankCard bankCard = new BankCard( cardNumber, 1L, LocalDate.of(2200, 10, 20), null, null);

        // Mock validación exitosa
        when(localClientRepository.updateBankCard(cardNumber, bankCard)).thenReturn(CompletableFuture.completedFuture(null));
        when(bankCardRepository.update(cardNumber, bankCard)).thenThrow(new RuntimeException("Error al actualizar tarjeta"));

        // Act & Assert
        BankCardNotFoundException exception = assertThrows(BankCardNotFoundException.class, () -> {
            clientService.updateBankCard(cardNumber, bankCard);
        });
        assertEquals("Tarjeta con numero "+ cardNumber+" no encontrada", exception.getMessage());

        verify(localClientRepository, times(1)).updateBankCard(cardNumber, bankCard);
        verify(bankCardRepository, times(1)).update(cardNumber, bankCard);
    }

    @Test
    void testDeleteBankCardSuccess() throws BankCardNotFoundException {
        // Arrange
        String cardNumber = "1234-5678-9012-3456";

        // Mock eliminación exitosa en repositorios
        when(bankCardRepository.delete(cardNumber)).thenReturn(CompletableFuture.completedFuture(null));
        when(localClientRepository.deleteBankCard(cardNumber)).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        clientService.deleteBankCard(cardNumber);

        // Assert
        verify(bankCardRepository, times(1)).delete(cardNumber);
        verify(localClientRepository, times(1)).deleteBankCard(cardNumber);
    }

    @Test
    void testDeleteBankCardThrowsException() {
        // Arrange
        String cardNumber = "1234-5678-9012-3456";
        when(bankCardRepository.delete(cardNumber)).thenThrow(new RuntimeException("Error al eliminar tarjeta"));

        // Act & Assert
        BankCardNotFoundException exception = assertThrows(BankCardNotFoundException.class, () -> {
            clientService.deleteBankCard(cardNumber);
        });

        assertEquals(cardNumber, exception.getMessage());
        verify(bankCardRepository, times(1)).delete(cardNumber);
        verify(localClientRepository, times(0)).deleteBankCard(anyString());
    }
}

