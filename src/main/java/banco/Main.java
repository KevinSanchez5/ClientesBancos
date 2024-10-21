package banco;


import banco.data.local.LocalDatabaseConfig;
import banco.data.local.LocalDatabaseInitializer;
import banco.data.local.LocalDatabaseManager;
import banco.data.remote.RemoteDatabaseManager;
import banco.domain.cards.repository.BankCardRepository;
import banco.domain.cards.repository.BankCardRepositoryImpl;
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

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        Logger logger = LoggerFactory.getLogger(Main.class);
        LocalDatabaseConfig localDatabaseConfig = new LocalDatabaseConfig("localclients/database.properties");
        LocalDatabaseManager localDatabaseManager = LocalDatabaseManager.getInstance();
        LocalDatabaseInitializer localDatabaseInitializer = new LocalDatabaseInitializer(localDatabaseConfig, localDatabaseManager);
        localDatabaseInitializer.initializeDatabase();
        RemoteDatabaseManager remoteDatabaseManager = RemoteDatabaseManager.getInstance();

        String baseUrl = "https://jsonplaceholder.typicode.com/users"; // Cambia esto a tu URL base
        Retrofit retrofit = RetrofitClient.getClient(baseUrl);
        ClientApiRest clientApiRest = retrofit.create(ClientApiRest.class);

        //Instancias para el servicio
        ClientRemoteRepository clientRemoteRepository = new ClientRemoteRepository(clientApiRest);
        ClientRepository localClientRepository = ImplClientRepository.getInstance(localDatabaseManager);
        BankCardRepository bankCardRepository = new BankCardRepositoryImpl(remoteDatabaseManager);
        NotificationService notificationService = new NotificationService();

        // Crear la instancia del servicio
        ImplClientService clientService = ImplClientService.getInstance(localClientRepository, bankCardRepository, clientRemoteRepository, notificationService);

        logger.debug(clientService.findAllClients().toString());

    }
}