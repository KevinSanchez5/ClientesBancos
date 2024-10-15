package banco.data.locale;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class LocalDatabaseInitializer {

    private final LocalDatabaseConfig config;

    public LocalDatabaseInitializer(LocalDatabaseConfig config) {
        this.config = config;
    }

    public void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(config.getUrl())) {
            System.out.println("Conexión a la base de datos establecida.");

            try (Statement stmt = conn.createStatement();
                 BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/data.sql"))) {

                String line;
                StringBuilder sql = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    sql.append(line).append("\n");
                }

                stmt.executeUpdate(sql.toString());
                System.out.println("Base de datos inicializada correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("Error en la conexión o ejecución SQL: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error al leer el archivo SQL: " + e.getMessage());
        }
    }
}
