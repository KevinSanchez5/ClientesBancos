package banco.data.locale;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class LocalDatabaseInitializer {

    private final LocalDatabaseConfig config;
    private final LocalDatabaseManager localDatabaseManager = LocalDatabaseManager.getInstance();

    public LocalDatabaseInitializer(LocalDatabaseConfig config) {
        this.config = config;
    }

    public void initializeDatabase() {
        try (Connection conn = localDatabaseManager.getConnection()) {
            System.out.println("Conexión a la base de datos establecida.");

            try (Statement stmt = conn.createStatement()) {
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
                stmt.executeUpdate(createClientsTable);
                System.out.println("Tabla 'clients' creada correctamente.");


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
                stmt.executeUpdate(createBankCardsTable);
                System.out.println("Tabla 'bank_cards' creada correctamente.");

            } catch (SQLException e) {
                System.err.println("Error al crear las tablas: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Error en la conexión: " + e.getMessage());
        }
    }
}
