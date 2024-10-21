package banco.domain.clients.service;

import banco.domain.cards.exceptions.BankCardException;
import banco.domain.cards.exceptions.BankCardNotFoundException;
import banco.domain.cards.model.BankCard;
import banco.domain.cards.repository.BankCardRepository;
import banco.domain.cards.validator.BankCardValidator;
import banco.domain.clients.exceptions.ClientExceptionBadRequest;
import banco.domain.clients.exceptions.ClientNotFound;
import banco.domain.clients.exceptions.ClientNotFoundException;
import banco.domain.clients.model.Client;
import banco.domain.clients.repository.ClientRemoteRepository;
import banco.domain.clients.service.cache.ClientesCache;
import banco.domain.clients.service.cache.ClientesCacheImpl;
import banco.domain.clients.service.notification.NotificationService;
import banco.domain.clients.model.notification.NotificationEvent;
import banco.domain.clients.model.notification.NotificationType;
import banco.domain.clients.repository.ClientRepository;
import banco.domain.clients.storage.json.ClientStorageJson;
import banco.domain.clients.validator.ClientValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.util.List;

public class ImplClientService implements ClientService {

    private final Logger logger = LoggerFactory.getLogger(ImplClientService.class);
    private static ImplClientService instance;

    //Repositorios
    private final ClientRepository localClientRepository;
    private final BankCardRepository bankCardRepository;
    private final ClientRemoteRepository remoteClientRepository;

    //Validadores, notificaciones y otros servicios
    private final NotificationService notificationService;
    private final ClientValidator clientValidator = new ClientValidator();
    private final BankCardValidator bankCardValidator = new BankCardValidator();
    private final ClientesCacheImpl cache = new ClientesCacheImpl(10);
    private final ClientStorageJson clientStorageJson = new ClientStorageJson();

    private ImplClientService(ClientRepository clientRepository, BankCardRepository bankCardRepository, ClientRemoteRepository clientRemoteRepository, NotificationService notificationService) {
        this.localClientRepository = clientRepository;
        this.bankCardRepository = bankCardRepository;
        this.remoteClientRepository = clientRemoteRepository;
        this.notificationService = notificationService;
    }

    public static synchronized ImplClientService getInstance(
            ClientRepository clientRepository,
            BankCardRepository bankCardRepository,
            ClientRemoteRepository clientRemoteRepository,
            NotificationService notificationService) {
        if (instance == null) {
            instance = new ImplClientService(clientRepository, bankCardRepository, clientRemoteRepository, notificationService);
        }
        return instance;
    }


    /**
     * Obtiene todos los clientes desde el repositorio remoto

     * @return
     */
    @Override
    public List<Client> findAllClients() {
        logger.debug("Buscando todos los clientes");
        return remoteClientRepository.getAll();
    }

    /**
     * Busca un cliente por id, primero en la caché, luego en el repositorio local y finalmente en el repositorio remoto
     * @param id
     * @return El cliente encontrado
     * @throws ClientNotFound si no se encuentra el cliente
     * @throws RuntimeException si ocurre un error al buscar el cliente
     */
    @Override
    public Client findByClientId(Long id) {
        logger.debug("Buscando cliente por id: " + id);
        Client cacheClient = cache.get(id);
        if (cacheClient != null) {
            logger.debug("Cliente encontrado en caché: " + cacheClient);
            return cacheClient;
        }
        logger.debug("Cliente no encontrado en caché, buscando en el repositorio local");
        Client localClient = localClientRepository.findById(id).join();
        if (localClient!= null) {
            logger.debug("Cliente encontrado en el repositorio local: " + localClient);
            cache.put(id, localClient);
            return localClient;
        }

        try {
            logger.debug("Cliente no encontrado en el repositorio local, buscando en el repositorio remoto");
            Client remoteClient = remoteClientRepository.getById(id.intValue());
            logger.debug("Cliente encontrado en el repositorio remoto: " + remoteClient);
            cache.put(id, remoteClient);
            localClientRepository.save(remoteClient).join();
            return remoteClient;
        } catch (ClientNotFound e) {
            logger.warn("Cliente no encontrado con id: " + id);
            throw new ClientNotFound(id.toString());
        } catch (Exception e) {
            logger.warn("Error al buscar cliente con id: " + id);
            throw new RuntimeException(e);
        }


    }

    /**
     * Busca un cliente con un id en la cache y en el local
     * Si no lo encuentra lo guarda en el repositorio remoto y envía una notificación
     * @param client cliente a guardar
     * @return el cliente guardado
     */
    @Override
    public Client saveClient(Client client) throws ClientExceptionBadRequest {
        clientValidator.validate(client);
        Client cacheClient = cache.get(client.getId());
        Client localClient = localClientRepository.findById(client.getId()).join();
        if (cacheClient == null && localClient == null) {
            try {
                logger.debug("Guardando cliente: {}", client);
                Client clientSaved = remoteClientRepository.createClient(client);
                localClientRepository.save(clientSaved).join();
                cache.put(clientSaved.getId(), clientSaved);
                NotificationEvent notificationEvent = new NotificationEvent(NotificationType.CREATE, clientSaved);
                notificationService.sendNotification(notificationEvent);
                return clientSaved;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if(cacheClient != null) {
            throw new ClientExceptionBadRequest("El cliente ya existe en la caché");
        } else {
            throw new ClientExceptionBadRequest("El cliente ya existe en el repositorio local");
        }
    }

    /**
     * Actualiza un cliente en la caché, en el repositorio local y en el remoto
     * @param id id del cliente a actualizar
     * @param client
     * @return el cliente actualizado
     * @throws ClientExceptionBadRequest
     */
    @Override
    public Client updateClient(Long id, Client client) throws ClientExceptionBadRequest {
        clientValidator.validate(client);
        try {
            // Actualiza en el repositorio remoto primero
            logger.debug("Actualizando cliente en repositorio remoto con id: {}", id);
            Client updatedRemoteClient = remoteClientRepository.updateClient(client);
            NotificationEvent notificationEvent = new NotificationEvent(NotificationType.UPDATE, updatedRemoteClient);
            notificationService.sendNotification(notificationEvent);
            logger.debug("Actualizando cliente en repositorio local y caché con id: {}", id);
            localClientRepository.save(updatedRemoteClient).join();
            cache.put(id, updatedRemoteClient);

            return updatedRemoteClient;
        } catch (Exception e) {
            logger.error("Error al actualizar el cliente con id: {}", id, e);
            throw new RuntimeException("Error al actualizar el cliente", e);
        }

    }

    /**
     * Elimina un cliente por id en el repositorio remoto, local y en la caché
     * @param id id del cliente a eliminar
     */
    @Override
    public void deleteClient(Long id) {
        try {
            logger.debug("Eliminando cliente con id: {}", id);
            remoteClientRepository.deleteClient(id.intValue());
            localClientRepository.delete(id).join();
            cache.remove(id);
            NotificationEvent notificationEvent = new NotificationEvent(NotificationType.DELETE, null);
            notificationService.sendNotification(notificationEvent);
        } catch (ClientNotFound e) {
            throw new ClientNotFound(id.toString());
        }catch (Exception e) {
            logger.error("Error al eliminar el cliente con id: {}", id, e);
            throw new RuntimeException("Error al eliminar el cliente con id: " + id, e);
        }

    }

    /**
     * Obtiene todas las tarjetas desde el repositorio de tarjetas bancarias
     * @return Lista de tarjetas
     */
    @Override
    public List<BankCard> findAllBankCards() {
        logger.debug("Buscando todas las tarjetas");
        return bankCardRepository.findAll().join();
    }

    /**
     * Busca todas las tarjetas de un cliente por id
     * @param id id del cliente
     * @return Lista de tarjetas
     */
    @Override
    public List<BankCard> findBankCardsByClientId(Long id) {
        logger.debug("Buscando tarjeta por id de cliente: " + id);
        return bankCardRepository.getBankCardsByClientId(id).join();
    }

    /**
     * Busca una tarjeta por número
     * @param number número de la tarjeta
     * @return la tarjeta encontrada
     */
    @Override
    public BankCard findBankCardByNumber(String number) {
        logger.debug("Buscando tarjeta por número: " + number);
        return bankCardRepository.findById(number).join();
    }

    /**
     * Guarda una tarjeta en el repositorio de tarjetas bancarias
     * @param bankCard tarjeta a guardar
     * @return la tarjeta guardada
     */
    @Override
    public BankCard saveBankCard(BankCard bankCard) {
        try {
            bankCardValidator.validate(bankCard);
            logger.debug("Guardando tarjeta: {}", bankCard);
            localClientRepository.saveBankCard(bankCard).join();
            return bankCardRepository.save(bankCard).join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Actualiza una tarjeta en el repositorio de tarjetas bancarias
     * @param number número de la tarjeta a actualizar
     * @param bankCard tarjeta actualizada
     * @return la tarjeta actualizada
     */
    @Override
    public BankCard updateBankCard(String number, BankCard bankCard) throws BankCardNotFoundException {
        try {
            bankCardValidator.validate(bankCard);
            logger.debug("Actualizando tarjeta con número: {}", number);
            localClientRepository.updateBankCard(number, bankCard).join();
            return bankCardRepository.update(number, bankCard).join();
        } catch (RuntimeException e) {
            throw new BankCardNotFoundException("Tarjeta con numero "+number+" no encontrada");
        } catch (BankCardException e) {
            logger.error("Error al actualizar la tarjeta: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Elimina una tarjeta por número
     * @param number número de la tarjeta a eliminar
     */
    @Override
    public void deleteBankCard(String number) throws BankCardNotFoundException {
        try {
            logger.debug("Eliminando tarjeta con número: {}", number);
            bankCardRepository.delete(number).join();
            localClientRepository.deleteBankCard(number).join();
        } catch (Exception e) {
            throw new BankCardNotFoundException(number);
        }
    }

    /**
     * Importa clientes desde un archivo JSON y llama al método para guardarlos uno por uno
     * @param file
     * @return Mono<Void>
     */
    public Mono<Void> importClientsFromJsonFile(File file) {
        return clientStorageJson.importFileMultipleClients(file)
                .flatMap(clients -> {
                    return Mono.when(
                            saveClientsIndividually(clients)
                    );
                })
                .then();
    }

    /**
     * Guarda los clientes uno en uno a partir de una lista de clientes
     * @param clients lista de clientes
     * @return Mono<Void>
     */
    private Mono<Void> saveClientsIndividually(List<Client> clients) {
        return Mono.<Void>fromRunnable(() -> {
            for (Client client : clients) {
                try {
                    // Validar el cliente
                    clientValidator.validate(client);

                    // Guardar en el repositorio local
                    localClientRepository.save(client).join();

                    // Guardar en el repositorio remoto
                    remoteClientRepository.createClient(client);

                } catch (ClientExceptionBadRequest e) {
                    // Manejar el caso donde la validación falla
                    logger.error("Validation failed for client {}: {}", client.getId(), e.getMessage());
                } catch (Exception e) {
                    // Manejar el caso donde el guardado falla
                    logger.error("Error saving client {}: {}", client.getId(), e.getMessage());
                }
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}


