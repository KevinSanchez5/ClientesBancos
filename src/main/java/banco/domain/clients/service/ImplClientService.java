package banco.domain.clients.service;

import banco.domain.clients.exceptions.ClientNotFound;
import banco.domain.clients.model.Client;
import banco.domain.clients.service.notification.NotificationService;
import banco.domain.clients.model.notification.NotificationEvent;
import banco.domain.clients.model.notification.NotificationType;
import banco.domain.clients.repository.ClientRepository;
import banco.domain.clients.validator.ClientValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ImplClientService implements ClientService {

    private final Logger logger = LoggerFactory.getLogger(ImplClientService.class);
    private static ImplClientService instance;
    //Acciones de consulta temporal en este repositorio hasta que este el remoto
    private final ClientRepository clientRepository;
    private final NotificationService notificationService = new NotificationService();
    private final ClientValidator clientValidator = new ClientValidator();

    public ImplClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public static synchronized ImplClientService getInstance(ClientRepository clientRepository) {
        if (instance == null) {
            instance = new ImplClientService(clientRepository);
        }
        return instance;
    }


    @Override
    public List<Client> findAll() {
        logger.debug("Buscando todos los clientes");
        return clientRepository.findAll().join();

    }

    @Override
    public Client findById(Long id) {
        logger.debug("Buscando cliente por id: " + id);
        try {
            return clientRepository.findById(id).join();
        }catch (Exception e) {
            throw new ClientNotFound(id.toString());
        }
    }

    @Override
    public Client save(Client client) {
        try{
            Client validClient = clientValidator.validate(client);
            logger.debug("Guardando cliente: {}", client.toString());
            Client clientSaved = clientRepository.save(validClient).join();
            NotificationEvent notificationEvent = new NotificationEvent(NotificationType.CREATE,clientSaved);
            notificationService.sendNotification(notificationEvent);
            return clientSaved;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Client update(Long id, Client client) {
        try {
            logger.debug("Actualizando cliente con id: {}", id);
            Client clientUpdated = clientRepository.update(id, client).join();
            NotificationEvent notificationEvent = new NotificationEvent(NotificationType.UPDATE,clientUpdated);
            notificationService.sendNotification(notificationEvent);
            return clientUpdated;
        }catch (RuntimeException e) {
            //TODO CAMBIAR EL TIPO DE EXCEPCION
            throw new ClientNotFound(id.toString());
        }
    }

    @Override
    public void delete(Long id) {
        try {
            logger.debug("Eliminando cliente con id: {}", id);
            clientRepository.delete(id).join();
            NotificationEvent notificationEvent = new NotificationEvent(NotificationType.DELETE, null);
            notificationService.sendNotification(notificationEvent);
        }catch (Exception e) {
            throw new ClientNotFound(id.toString());
        }

    }
}
