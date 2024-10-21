package banco;


import banco.data.local.LocalDatabaseConfig;
import banco.data.local.LocalDatabaseInitializer;
import banco.data.local.LocalDatabaseManager;
import banco.data.remote.RemoteDatabaseManager;
import banco.domain.cards.model.BankCard;
import banco.domain.cards.repository.BankCardRepository;
import banco.domain.cards.repository.BankCardRepositoryImpl;
import banco.domain.clients.exceptions.ClientExceptionBadRequest;
import banco.domain.clients.model.Client;
import banco.domain.clients.repository.ClientRemoteRepository;
import banco.domain.clients.repository.ClientRepository;
import banco.domain.clients.repository.ImplClientRepository;
import banco.domain.clients.rest.ClientApiRest;
import banco.domain.clients.rest.RetrofitClient;
import banco.domain.clients.service.ImplClientService;
import banco.domain.clients.service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;

import java.awt.desktop.SystemSleepEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, ClientExceptionBadRequest {

        Logger logger = LoggerFactory.getLogger(Main.class);
        LocalDatabaseConfig localDatabaseConfig = new LocalDatabaseConfig("localclients/database.properties");
        TimeUnit.SECONDS.sleep(10);
        LocalDatabaseManager localDatabaseManager = LocalDatabaseManager.getInstance();
        LocalDatabaseInitializer localDatabaseInitializer = new LocalDatabaseInitializer(localDatabaseConfig, localDatabaseManager);
        localDatabaseInitializer.initializeDatabase();
        RemoteDatabaseManager remoteDatabaseManager = RemoteDatabaseManager.getInstance();

        String baseUrl = ClientApiRest.API_CLIENTS_URL; // Cambia esto a tu URL base
        Retrofit retrofit = RetrofitClient.getClient(baseUrl);
        ClientApiRest clientApiRest = retrofit.create(ClientApiRest.class);

        //Instancias para el servicio
        ClientRemoteRepository clientRemoteRepository = new ClientRemoteRepository(clientApiRest);
        ClientRepository localClientRepository = ImplClientRepository.getInstance(localDatabaseManager);
        BankCardRepository bankCardRepository = new BankCardRepositoryImpl(remoteDatabaseManager);
        NotificationService notificationService = new NotificationService();

        // Crear la instancia del servicio
        ImplClientService clientService = ImplClientService.getInstance(localClientRepository, bankCardRepository, clientRemoteRepository, notificationService);

        System.out.println(clientService.saveClient(
                Client.builder()
                        .id(1L)
                        .name("Pepe")
                        .email("pepe@gmail.com")
                        .cards(Collections.emptyList())
                        .username("pepe")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build())
        );
        logger.debug(clientService.findAllClients().toString());

        System.out.println(bankCardRepository.save(
                BankCard.builder()
                        .number("9876543210987654")
                        .clientId(1L)
                        .expirationDate(LocalDate.now())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        ).get());
    }
}