package banco.domain.clients.repository;

import banco.data.local.LocalDatabaseManager;
import banco.domain.cards.model.BankCard;
import banco.domain.clients.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.sql.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementación del repositorio local de clientes
 *
 * @constructor crea una instancia con el LocalDataBaseManager
 */
public class ImplClientRepository implements ClientRepository {

    private final Logger logger = LoggerFactory.getLogger(ImplClientRepository.class);
    private static ImplClientRepository instance;
    private final LocalDatabaseManager localDatabase;


    private final ExecutorService executorService = Executors.newCachedThreadPool();


    public static synchronized ImplClientRepository getInstance(LocalDatabaseManager local) {
        if (instance == null) {
            instance = new ImplClientRepository(local);
        }
        return instance;
    }

    /**
     * Constructor privado
     * @param localDatabase el manejador de la base de datos local
     */
    private ImplClientRepository(LocalDatabaseManager localDatabase) {
        this.localDatabase = localDatabase;
    }


    /**
     * Busca todos los clientes en la base de datos local.
     *
     * @return CompletableFuture de una liste de clientes
     * o una lista vacia si no hay nada
     */
    @Override
    public CompletableFuture<List<Client>> findAll() {
        logger.debug("Buscando todos los clientes");
        String sql = "SELECT id, name, username, email, created_at, updated_at FROM clients";

        return CompletableFuture.supplyAsync(() -> {
            List<Client> clients = new ArrayList<>();

            try (Connection conn = localDatabase.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Long clientId = rs.getLong("id");

                    Client client = new Client(
                            clientId,
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("email"),
                            new ArrayList<>(),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                    List<BankCard> cards = findAllCardsByClientId(clientId);
                    client.setCards(cards);
                    clients.add(client);
                }
            } catch (SQLException e) {
                logger.error("Error al buscar todos los clientes", e);
            }
            return clients;
        }, executorService);
    }

    /**
     * Busca un cliente por su id en la base de datos local.
     *
     * @param id el id del cliente a buscar
     * @return CompletableFuture de un cliente
     * o null si no se encuentra
     */
    public CompletableFuture<Client> findById(Long id) {
        logger.debug("Buscando cliente con id: {}", id);
        String query = "SELECT * FROM clients WHERE id =?";
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = localDatabase.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setLong(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Client client = new Client(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("email"),
                            new ArrayList<>(),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );

                    List<BankCard> cards = findAllCardsByClientId(id);
                    client.setCards(cards);
                    return client;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Guarda un cliente en la base de datos local.
     * @param client el cliente a guardar
     * @return CompletableFuture de un cliente
     */
    @Override
    public CompletableFuture<Client> save(Client client) {
        logger.debug("Guardando cliente: {}", client);
        return CompletableFuture.supplyAsync(() -> {
            String clientSql = "INSERT INTO clients (name, username, email, created_at, updated_at) VALUES (?, ?, ?, ?, ?) RETURNING id";
            Long clientId = null;

            try (Connection conn = localDatabase.getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement clientStmt = conn.prepareStatement(clientSql)) {
                    clientStmt.setString(1, client.getName());
                    clientStmt.setString(2, client.getUsername());
                    clientStmt.setString(3, client.getEmail());
                    clientStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    clientStmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

                    try (ResultSet rs = clientStmt.executeQuery()) {
                        if (rs.next()) {
                            clientId = rs.getLong("id");
                        }
                    }
                    if (clientId != null) {
                        for (BankCard card : client.getCards()) {
                            String cardSql = "INSERT INTO bank_cards (number, client_id, expiration_date, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
                            try (PreparedStatement cardStmt = conn.prepareStatement(cardSql)) {
                                cardStmt.setString(1, card.getNumber());
                                cardStmt.setLong(2, clientId);
                                cardStmt.setDate(3, Date.valueOf(card.getExpirationDate()));
                                cardStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                                cardStmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                                cardStmt.executeUpdate();
                            }
                        }
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    logger.error("Error al guardar cliente: {}", client, e);
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                logger.error("Error en la transacción de guardado de cliente", e);
            }
            if (clientId != null) {
                client.setId(clientId);
            }
            return client;
        }, executorService);
    }

    /**
     * Actualiza un cliente en la base de datos local.
     * @param id el id del cliente a actualizar
     * @param client el cliente con los datos actualizados
     * @return CompletableFuture de un cliente
     * o null si no se actualiza
     */
    @Override
    public CompletableFuture<Client> update(Long id, Client client) {
        logger.debug("Actualizando cliente con id: {} con datos: {}", id, client);
        return CompletableFuture.supplyAsync(() -> {
            String clientSql = "UPDATE clients SET name = ?, username = ?, email = ?, updated_at = ? WHERE id = ?";
            boolean updatedClient = false;

            try (Connection conn = localDatabase.getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement clientStmt = conn.prepareStatement(clientSql)) {
                    clientStmt.setString(1, client.getName());
                    clientStmt.setString(2, client.getUsername());
                    clientStmt.setString(3, client.getEmail());
                    clientStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    clientStmt.setLong(5, id);

                    updatedClient = clientStmt.executeUpdate() > 0;

                    if (updatedClient) {
                        String deleteCardsSql = "DELETE FROM bank_cards WHERE client_id = ?";
                        try (PreparedStatement deleteCardsStmt = conn.prepareStatement(deleteCardsSql)) {
                            deleteCardsStmt.setLong(1, id);
                            deleteCardsStmt.executeUpdate();
                        }

                        for (BankCard card : client.getCards()) {
                            String cardSql = "INSERT INTO bank_cards (number, client_id, expiration_date, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
                            try (PreparedStatement cardStmt = conn.prepareStatement(cardSql)) {
                                cardStmt.setString(1, card.getNumber());
                                cardStmt.setLong(2, id);
                                cardStmt.setDate(3, Date.valueOf(card.getExpirationDate()));
                                cardStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                                cardStmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                                cardStmt.executeUpdate();
                            }
                        }
                    }

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    logger.error("Error al actualizar cliente con id: {}", id, e);
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }

            } catch (SQLException e) {
                logger.error("Error en la transacción de actualización del cliente", e);
            }

            return updatedClient ? client : null;
        }, executorService);
    }

    /**
     * Elimina un cliente de la base de datos local.
     * @param id el id del cliente a eliminar
     * @return CompletableFuture de void
     */
    @Override
    public CompletableFuture<Void> delete(Long id) {
        logger.debug("Eliminando cliente con id: {}", id);
        return CompletableFuture.runAsync(() -> {
            Connection conn = null;
            try {
                conn = localDatabase.getConnection();
                conn.setAutoCommit(false);

                String clientSql = "DELETE FROM clients WHERE id = ?";
                try (PreparedStatement clientStmt = conn.prepareStatement(clientSql)) {
                    clientStmt.setLong(1, id);
                    clientStmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                logger.error("Error al eliminar cliente con id: {}", id, e);
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        logger.error("Error durante rollback al eliminar cliente con id: {}", id, rollbackEx);
                    }
                }
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close(); // Cerrar conexión
                    } catch (SQLException ex) {
                        logger.error("Error al restaurar auto-commit o cerrar conexión después de eliminar cliente con id: {}", id, ex);
                    }
                }
            }
        }, executorService);
    }


    /**
     * Busca todas las tarjetas de un cliente por su id en la base de datos local.
     * @param clientId el id del cliente
     * @return lista de tarjetas
     */
    public List<BankCard> findAllCardsByClientId(Long clientId) {
        List<BankCard> cards = new ArrayList<>();
        try (var conn = localDatabase.getConnection();
             var stmt = conn.prepareStatement("SELECT * FROM bank_cards WHERE client_id = ?")) {
            stmt.setLong(1, clientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BankCard card = new BankCard(
                        rs.getString("number"),
                        rs.getLong("client_id"),
                        rs.getDate("expiration_date").toLocalDate(),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
                cards.add(card);
            }

        } catch (SQLException e) {
            logger.error("Error al recuperar las tarjetas del cliente con ID " + clientId);
            e.printStackTrace();
        }

        return cards;
    }

    /**
     * Guarda una tarjeta de crédito en la base de datos local.
     * @param bankCard la tarjeta de crédito a guardar
     * @return CompletableFuture de la tarjeta de crédito guardada
     */
    public CompletableFuture<BankCard> saveBankCard(BankCard bankCard) {
        logger.debug("Guardando tarjeta: {}", bankCard);
        String sql = "INSERT INTO bank_cards (number, client_id, expiration_date) VALUES (?, ?, ?)";
        return CompletableFuture.supplyAsync(() -> {

            try (var conn = localDatabase.getConnection();
                 var stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, bankCard.getNumber());
                stmt.setLong(2, bankCard.getClientId());
                stmt.setDate(3, java.sql.Date.valueOf(bankCard.getExpirationDate()));

                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error al guardar tarjeta: {}", bankCard, e);
            }
            return bankCard;
        }, executorService);
    }

    /**
     * Actualiza una tarjeta de crédito en la base de datos local.
     * @param cardNumber el número de la tarjeta a actualizar
     * @param updatedBankCard la tarjeta de crédito con los datos actualizados
     * @return CompletableFuture de void
     */
    public CompletableFuture<Void> updateBankCard(String cardNumber, BankCard updatedBankCard) {
        logger.debug("Actualizando tarjeta con número: {}", cardNumber);
        String query = "UPDATE bank_cards SET number = ?, client_id = ?, expiration_date = ? WHERE number = ?";

        return CompletableFuture.runAsync(() -> {
            try (Connection conn = localDatabase.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, updatedBankCard.getNumber());
                stmt.setLong(2, updatedBankCard.getClientId());
                stmt.setDate(3, java.sql.Date.valueOf(updatedBankCard.getExpirationDate()));
                stmt.setString(4, cardNumber); // El número de tarjeta existente

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    logger.warn("No se encontró ninguna tarjeta con el número: {}", cardNumber);
                }
            } catch (SQLException e) {
                logger.error("Error al actualizar tarjeta con número: {}", cardNumber, e);
            }
        }, executorService);
    }

    /**
     * Elimina una tarjeta de crédito de la base de datos local.
     * @param cardNumber el número de la tarjeta a eliminar
     * @return CompletableFuture de void
     */
    public CompletableFuture<Void> deleteBankCard(String cardNumber) {
        logger.debug("Borrando tarjeta con número: {}", cardNumber);
        String query = "DELETE FROM bank_cards WHERE number = ?";

        return CompletableFuture.runAsync(() -> {
            try (Connection conn = localDatabase.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, cardNumber);
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted == 0) {
                    logger.warn("No se encontró ninguna tarjeta con el número: {}", cardNumber);
                } else {
                    logger.info("Tarjeta con número: {} ha sido borrada exitosamente.", cardNumber);
                }
            } catch (SQLException e) {
                logger.error("Error al borrar la tarjeta con número: {}", cardNumber, e);
            }
        }, executorService);
    }

    /**
     * Refresca los clientes y las tarjetas en la base de datos local.
     * @param clients lista de clientes
     * @param bankCards lista de tarjetas
     * @return CompletableFuture de void
     */
    public CompletableFuture<Void> refreshClientsAndCards(List<Client> clients, List<BankCard> bankCards) {
        logger.debug("Refrescando clientes y tarjetas");
        return CompletableFuture.runAsync(() -> {
            Connection conn = null;
            try {
                conn = localDatabase.getConnection();
                conn.setAutoCommit(false);

                // Eliminar todas las tarjetas
                String deleteCardsSql = "DELETE FROM bank_cards";
                try (PreparedStatement deleteCardsStmt = conn.prepareStatement(deleteCardsSql)) {
                    deleteCardsStmt.executeUpdate();
                }

                // Eliminar todos los clientes
                String deleteClientsSql = "DELETE FROM clients";
                try (PreparedStatement deleteClientsStmt = conn.prepareStatement(deleteClientsSql)) {
                    deleteClientsStmt.executeUpdate();
                }

                // Insertar nuevos clientes
                String clientSql = "INSERT INTO clients (name, username, email, created_at, updated_at) VALUES (?, ?, ?, ?, ?) RETURNING id";
                for (Client client : clients) {
                    Long clientId = null;
                    try (PreparedStatement clientStmt = conn.prepareStatement(clientSql)) {
                        clientStmt.setString(1, client.getName());
                        clientStmt.setString(2, client.getUsername());
                        clientStmt.setString(3, client.getEmail());
                        clientStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                        clientStmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

                        try (ResultSet rs = clientStmt.executeQuery()) {
                            if (rs.next()) {
                                clientId = rs.getLong("id");
                            }
                        }
                    }

                    // Insertar las tarjetas asociadas al cliente
                    if (clientId != null) {
                        String cardSql = "INSERT INTO bank_cards (number, client_id, expiration_date, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
                        for (BankCard card : client.getCards()) {
                            try (PreparedStatement cardStmt = conn.prepareStatement(cardSql)) {
                                cardStmt.setString(1, card.getNumber());
                                cardStmt.setLong(2, clientId);
                                cardStmt.setDate(3, Date.valueOf(card.getExpirationDate()));
                                cardStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                                cardStmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                                cardStmt.executeUpdate();
                            }
                        }
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                logger.error("Error al refrescar clientes y tarjetas", e);
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        logger.error("Error durante el rollback al refrescar clientes y tarjetas", rollbackEx);
                    }
                }
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close(); // Cerrar conexión
                    } catch (SQLException ex) {
                        logger.error("Error al restaurar auto-commit o cerrar conexión después de refrescar clientes y tarjetas", ex);
                    }
                }
            }
        }, executorService);
    }

// Para ejecutar el refresco
//    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//scheduler.scheduleAtFixedRate(() -> {
//        List<Client> newClients = remoteClientRepository.findAll();
//                List<BankCard> newCards = remoteBankCardRepository.findAll();
//                        clientRepository.refreshClientsAndCards(newClients, newCards);
//    }, 0, 30, TimeUnit.SECONDS);

}
