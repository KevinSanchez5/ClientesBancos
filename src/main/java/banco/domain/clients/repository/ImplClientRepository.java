package banco.domain.clients.repository;

import banco.data.locale.LocalDatabaseManager;
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

public class ImplClientRepository implements ClientRepository {

    private final Logger logger = LoggerFactory.getLogger(ImplClientRepository.class);
    public static ImplClientRepository instance;
    private final LocalDatabaseManager localDatabase = LocalDatabaseManager.getInstance();


    private final ExecutorService executorService = Executors.newCachedThreadPool();


    public static synchronized ImplClientRepository getInstance() {
        if (instance == null) {
            instance = new ImplClientRepository();
        }
        return instance;
    }

    @Override
    public CompletableFuture<List<Client>> findAll() {
        logger.debug("Buscando todos los clientes");
        String sql = "SELECT c.id, c.name, c.username, c.email, c.created_at, c.updated_at, "
                + "bc.number, bc.expiration_date, bc.created_at as card_created_at, bc.updated_at as card_updated_at "
                + "FROM clients c "
                + "LEFT JOIN bank_cards bc ON c.id = bc.client_id";

        return CompletableFuture.supplyAsync(() -> {

            List<Client> clients = new ArrayList<>();
            Map<Long, Client> clientMap = new HashMap<>();

            try (Connection conn = localDatabase.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Long clientId = rs.getLong("id");

                    // Si el cliente no existe en el mapa, lo creamos
                    if (!clientMap.containsKey(clientId)) {
                        Client client = new Client(
                                clientId,
                                rs.getString("name"),
                                rs.getString("username"),
                                rs.getString("email"),
                                new ArrayList<>(), // Inicialmente vacía
                                rs.getTimestamp("created_at").toLocalDateTime(),
                                rs.getTimestamp("updated_at").toLocalDateTime()
                        );
                        clientMap.put(clientId, client);
                        clients.add(client);
                    }

                    String cardNumber = rs.getString("number");
                    if (cardNumber != null) {
                        BankCard card = new BankCard(
                                cardNumber,
                                clientMap.get(clientId), // Usar el cliente del mapa
                                rs.getDate("expiration_date").toLocalDate(),
                                rs.getTimestamp("card_created_at").toLocalDateTime(),
                                rs.getTimestamp("card_updated_at").toLocalDateTime()
                        );
                        clientMap.get(clientId).getCards().add(card);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error al buscar todos los clientes", e);
            }

            return clients;
        }, executorService);
    }


    public CompletableFuture<Client> findById(Long id) {
        logger.debug("Buscando cliente con id: {}", id);
        String sql = "SELECT c.id, c.name, c.username, c.email, c.created_at, c.updated_at, "
                + "bc.number, bc.expiration_date, bc.created_at as card_created_at, bc.updated_at as card_updated_at "
                + "FROM clients c "
                + "LEFT JOIN bank_cards bc ON c.id = bc.client_id "
                + "WHERE c.id = ?";

        return CompletableFuture.supplyAsync(() -> {
            Client client = null;
            List<BankCard> cards = new ArrayList<>();

            try (Connection conn = localDatabase.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        if (client == null) {
                            client = new Client(
                                    rs.getLong("id"),
                                    rs.getString("name"),
                                    rs.getString("username"),
                                    rs.getString("email"),
                                    new ArrayList<>(),
                                    rs.getTimestamp("created_at").toLocalDateTime(),
                                    rs.getTimestamp("updated_at").toLocalDateTime()
                            );
                        }

                        String cardNumber = rs.getString("number");
                        if (cardNumber != null) {
                            BankCard card = new BankCard(
                                    cardNumber,
                                    client, // Cliente asociado a la tarjeta
                                    rs.getDate("expiration_date").toLocalDate(),
                                    rs.getTimestamp("card_created_at").toLocalDateTime(),
                                    rs.getTimestamp("card_updated_at").toLocalDateTime()
                            );
                            cards.add(card);
                        }
                    }
                }
                if (client != null) {
                    client.setCards(cards);
                }

            } catch (SQLException e) {
                logger.error("Error al buscar cliente con id: {}", id, e);
            }

            return client;
        }, executorService);
    }

    @Override
    public CompletableFuture<Client> save(Client client) {
        logger.debug("Guardando cliente: {}", client);
        return CompletableFuture.supplyAsync(() -> {
            String clientSql = "INSERT INTO clients (name, username, email, created_at, updated_at) VALUES (?, ?, ?, ?, ?) RETURNING id";
            Long clientId = null;

            try (Connection conn = localDatabase.getConnection();
                 PreparedStatement clientStmt = conn.prepareStatement(clientSql)) {

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

            } catch (SQLException e) {
                logger.error("Error al guardar cliente: {}", client, e);
            }

            if (clientId != null) {
                client.setId(clientId);
            }
            return client;
        }, executorService);
    }


    @Override
    public CompletableFuture<Client> update(Long id, Client client) {
        logger.debug("Actualizando cliente con id: {} con datos: {}", id, client);
        return CompletableFuture.supplyAsync(() -> {
            String clientSql = "UPDATE clients SET name = ?, username = ?, email = ?, updated_at = ? WHERE id = ?";
            boolean updatedClient = false;

            try (Connection conn = localDatabase.getConnection();
                 PreparedStatement clientStmt = conn.prepareStatement(clientSql)) {

                clientStmt.setString(1, client.getName());
                clientStmt.setString(2, client.getUsername());
                clientStmt.setString(3, client.getEmail());
                clientStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                clientStmt.setLong(5, id); // Usamos el id proporcionado

                updatedClient = clientStmt.executeUpdate() > 0;

                if (updatedClient) {
                    String deleteCardsSql = "DELETE FROM bank_cards WHERE client_id = ?";
                    try (PreparedStatement deleteCardsStmt = conn.prepareStatement(deleteCardsSql)) {
                        deleteCardsStmt.setLong(1, id); // Usar el id proporcionado
                        deleteCardsStmt.executeUpdate();
                    }

                    for (BankCard card : client.getCards()) {
                        String cardSql = "INSERT INTO bank_cards (number, client_id, expiration_date, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement cardStmt = conn.prepareStatement(cardSql)) {
                            cardStmt.setString(1, card.getNumber());
                            cardStmt.setLong(2, id); // Usar el id proporcionado
                            cardStmt.setDate(3, Date.valueOf(card.getExpirationDate()));
                            cardStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                            cardStmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                            cardStmt.executeUpdate();
                        }
                    }
                }

            } catch (SQLException e) {
                logger.error("Error al actualizar cliente con id: {}", id, e);
            }

            return updatedClient ? client : null; // Devolver el cliente actualizado o null si no se actualizó
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> delete(Long id) {
        logger.debug("Eliminando cliente con id: {}", id);
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = localDatabase.getConnection()) {
                String deleteCardsSql = "DELETE FROM bank_cards WHERE client_id = ?";
                try (PreparedStatement deleteCardsStmt = conn.prepareStatement(deleteCardsSql)) {
                    deleteCardsStmt.setLong(1, id);
                    deleteCardsStmt.executeUpdate();
                }

                String clientSql = "DELETE FROM clients WHERE id = ?";
                try (PreparedStatement clientStmt = conn.prepareStatement(clientSql)) {
                    clientStmt.setLong(1, id);
                    clientStmt.executeUpdate();
                }

            } catch (SQLException e) {
                logger.error("Error al eliminar cliente con id: {}", id, e);
            }
        }, executorService);
    }
}
