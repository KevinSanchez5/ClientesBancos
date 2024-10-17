package banco.data.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LocalDatabaseInitializer {

    private Logger logger = LoggerFactory.getLogger(LocalDatabaseInitializer.class);
    private final LocalDatabaseConfig config;
    private final LocalDatabaseManager localDatabaseManager;

    public LocalDatabaseInitializer(LocalDatabaseConfig config, LocalDatabaseManager localDatabaseManager) {
        this.config = config;
        this.localDatabaseManager = localDatabaseManager;
    }

    public void initializeDatabase() {
        try (Connection conn = localDatabaseManager.getConnection()) {
            logger.debug("Conexión establecida.");
            createClientsTable(conn);
            createBankCardsTable(conn);
        } catch (SQLException e) {
            logger.error("Error en la conexión: " + e.getMessage());
        }
    }


    private void createClientsTable(Connection conn) {
        String createClientsTable = """
            CREATE TABLE IF NOT EXISTS clients (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(255),
                username VARCHAR(255) NOT NULL UNIQUE,
                email VARCHAR(255) NOT NULL UNIQUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createClientsTable);
            logger.debug("Tabla 'clients' creada.");
        } catch (SQLException e) {
            logger.error("Error al crear la tabla 'clients': " + e.getMessage());
        }
    }


    private void createBankCardsTable(Connection conn) {
        String createBankCardsTable = """
            CREATE TABLE IF NOT EXISTS bank_cards (
                number VARCHAR(255) PRIMARY KEY,
                client_id INTEGER NOT NULL,
                expiration_date DATE NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createBankCardsTable);
            logger.debug("Tabla 'bank_cards' creada.");
        } catch (SQLException e) {
            System.err.println("Error al crear la tabla 'bank_cards': " + e.getMessage());
        }
    }

    public void listTables() {
        String query = "SELECT name FROM sqlite_master WHERE type='table';";

        try (Connection conn = localDatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("Tablas en la base de datos:");
            while (rs.next()) {
                System.out.println(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar las tablas: " + e.getMessage());
        }
    }
}

