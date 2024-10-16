package banco.domain.cards.repository;

import banco.domain.cards.exceptions.BankCardNotFoundException;
import banco.domain.cards.exceptions.BankCardNotSavedException;
import banco.domain.cards.model.BankCard;
import banco.domain.cards.database.RemoteDatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Implementación del repositorio de tarjetas de crédito.
 *
 * @property db El gestor de la base de datos remota utilizado para manejar conexiones y ejecutar consultas.
 * @constructor Crea una instancia de `BankCardRepositoryImpl` con el gestor de base de datos proporcionado.
 */
public class BankCardRepositoryImpl implements BankCardRepository {
    private final Logger logger = LoggerFactory.getLogger(BankCardRepositoryImpl.class);
    private final RemoteDatabaseManager db;
    private static BankCardRepositoryImpl instance;

    /**
     * Constructor para la clase `BankCardRepositoryImpl`.
     *
     * @param db El gestor de base de datos remota proporcionado.
     */
    public BankCardRepositoryImpl(RemoteDatabaseManager db) {
        this.db = db;
    }

    /**
     * Devuelve una instancia única de `BankCardRepositoryImpl` utilizando el patrón Singleton.
     *
     * @param db El gestor de base de datos remota proporcionado.
     * @return La instancia única de `BankCardRepositoryImpl`.
     */
    public static BankCardRepositoryImpl getInstance(RemoteDatabaseManager db) {
        if (instance == null) {
            instance = new BankCardRepositoryImpl(db);
        }
        return instance;
    }

    /**
     * Busca todas las tarjetas de crédito en la base de datos.
     *
     * @return Un `CompletableFuture` que contiene una lista de todas las tarjetas de crédito encontradas,
     * o una lista vacía si no se encuentra ninguna.
     */
    @Override
    public CompletableFuture<List<BankCard>> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            List<BankCard> lista = new ArrayList<>();
            String query = "SELECT * FROM BankCards";
            logger.debug("Buscando todas las tarjetas de crédito en la base de datos");

            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query);
                 var rs = stmt.executeQuery()) {

                while (rs.next()) {
                    BankCard bankCard = BankCard.builder()
                            .number(rs.getString("number"))
                            .clientId(rs.getObject("clientId", Long.class))
                            .expirationDate(rs.getObject("expirationDate", LocalDate.class))
                            .createdAt(rs.getObject("createdAt", LocalDateTime.class))
                            .updatedAt(rs.getObject("updatedAt", LocalDateTime.class))
                            .build();
                    lista.add(bankCard);
                }
            } catch (SQLException e) {
                logger.error("Error al buscar todas las tarjetas de crédito", e);
                throw new CompletionException(e);
            }
            return lista;
        });
    }

    /**
     * Busca una tarjeta de crédito por su UUID.
     *
     * @param id El número de la tarjeta de crédito a buscar.
     * @return Un `CompletableFuture` que contiene la tarjeta de crédito si se encuentra, o `null` si no existe.
     */
    @Override
    public CompletableFuture<BankCard> findById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            BankCard bankCard = null;
            String query = "SELECT * FROM BankCards WHERE number = ?";
            logger.debug("Buscando la tarjeta de crédito con uuid: {}", id);

            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)) {
                stmt.setObject(1, id);
                var rs = stmt.executeQuery();
                if (rs.next()) {
                    bankCard = BankCard.builder()
                            .number(rs.getString("number"))
                            .clientId(rs.getObject("clientId", Long.class))
                            .expirationDate(rs.getObject("expirationDate", LocalDate.class))
                            .createdAt(rs.getObject("createdAt", LocalDateTime.class))
                            .updatedAt(rs.getObject("updatedAt", LocalDateTime.class))
                            .build();
                }
            } catch (SQLException e) {
                logger.error("Error al buscar la tarjeta de crédito por uuid", e);
                throw new CompletionException(e);
            }
            return bankCard;
        });
    }

    /**
     * Guarda una nueva tarjeta de crédito en la base de datos.
     *
     * @param bankCard La tarjeta de crédito a guardar.
     * @return Un `CompletableFuture` que contiene la tarjeta de crédito guardada, o lanza una excepción si hay un error.
     */
    @Override
    public CompletableFuture<BankCard> save(BankCard bankCard) {
        String query = "INSERT INTO BankCards (number, clientId, expirationDate, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?)";
        logger.debug("Guardando la tarjeta de crédito: {}", bankCard);

        return CompletableFuture.supplyAsync(() -> {
            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)) {
                stmt.setString(1, bankCard.getNumber());
                stmt.setObject(2, bankCard.getClientId());
                stmt.setObject(3, bankCard.getExpirationDate());
                stmt.setObject(4, bankCard.getCreatedAt());
                stmt.setObject(5, bankCard.getUpdatedAt());

                int res = stmt.executeUpdate();
                if (res == 0) {
                    logger.error("Tarjeta de crédito no guardada");
                    throw new BankCardNotSavedException("Tarjeta de crédito no guardada con id: " + bankCard.getNumber());
                }
            } catch (SQLException | BankCardNotSavedException e) {
                logger.error("Error al guardar la tarjeta de crédito", e);
                throw new CompletionException(e);
            }
            return bankCard;
        });
    }

    /**
     * Actualiza una tarjeta de crédito existente en la base de datos.
     *
     * @param id El número de la tarjeta a actualizar.
     * @param bankCard La tarjeta de crédito con los nuevos datos.
     * @return Un `CompletableFuture` que contiene la tarjeta de crédito actualizada, o lanza una excepción si no se encuentra.
     */
    @Override
    public CompletableFuture<BankCard> update(String id, BankCard bankCard) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "UPDATE BankCards SET expirationDate = ?, updatedAt = ? WHERE number = ?";
            logger.debug("Actualizando la tarjeta de crédito con uuid: {}", id);

            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)) {

                stmt.setObject(1, bankCard.getExpirationDate());
                stmt.setObject(2, LocalDateTime.now());
                stmt.setString(3, bankCard.getNumber());


                int res = stmt.executeUpdate();
                if (res > 0) {
                    logger.debug("Tarjeta de crédito actualizada");
                } else {
                    logger.error("Tarjeta de crédito no actualizada al no encontrarse en la base de datos con id: " + id);
                    throw new BankCardNotFoundException("Tarjeta de crédito no encontrada con id: " + id);
                }
            } catch (SQLException | BankCardNotFoundException e) {
                logger.error("Error al actualizar la tarjeta de crédito", e);
                throw new CompletionException(e);
            }
            return bankCard;
        });
    }

    /**
     * Elimina una tarjeta de crédito de la base de datos por su UUID.
     *
     * @param id El id de la tarjeta de crédito a eliminar.
     * @return Un `CompletableFuture` que contiene `true` si la tarjeta fue eliminada, o `false` si no se encontró.
     */
    @Override
    public CompletableFuture<Boolean> delete(String id) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "DELETE FROM BankCards WHERE number = ?";
            logger.debug("Eliminando la tarjeta de crédito con uuid: {}", id);

            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)) {

                stmt.setObject(1, id);
                int res = stmt.executeUpdate();
                return res > 0;
            } catch (SQLException e) {
                logger.error("Error al eliminar la tarjeta de crédito", e);
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Busca todas las tarjetas de crédito asociadas a un cliente.
     *
     * @param clientId El UUID del cliente.
     * @return Un `CompletableFuture` que contiene una lista de tarjetas de crédito asociadas al cliente, o una lista vacía si no se encuentran.
     */
    @Override
    public CompletableFuture<List<BankCard>> getBankCardsByClientId(Long clientId) {
        return CompletableFuture.supplyAsync(() -> {
            List<BankCard> lista = new ArrayList<>();
            String query = "SELECT * FROM BankCards WHERE clientId = ?";
            logger.debug("Buscando las tarjetas de crédito del cliente con uuid: {}", clientId);

            try (var connection = db.getConnection();
                 var stmt = connection.prepareStatement(query)) {

                stmt.setObject(1, clientId);
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    BankCard bankCard = BankCard.builder()
                            .number(rs.getString("number"))
                            .clientId(rs.getObject("clientId", Long.class))
                            .expirationDate(rs.getObject("expirationDate", LocalDate.class))
                            .createdAt(rs.getObject("createdAt", LocalDateTime.class))
                            .updatedAt(rs.getObject("updatedAt", LocalDateTime.class))
                            .build();
                    lista.add(bankCard);
                }
            } catch (SQLException e) {
                logger.error("Error al buscar las tarjetas de crédito por cliente", e);
                throw new CompletionException(e);
            }
            return lista;
        });
    }
}


