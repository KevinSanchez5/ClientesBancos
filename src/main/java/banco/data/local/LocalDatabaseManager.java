package banco.data.local;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


public class LocalDatabaseManager {
    private static LocalDatabaseManager instance;
    private HikariDataSource dataSource;


    public static synchronized LocalDatabaseManager getInstance() {
        if (instance == null) {
            instance = new LocalDatabaseManager();
        }
        return instance;
    }


    // Constructor privado para el Singleton
    private LocalDatabaseManager() {
        try {
            Properties properties = new Properties();
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("localclients/database.properties")) {
                if (input == null) {
                    throw new RuntimeException("No se pudo encontrar el archivo database.properties");
                }
                properties.load(input);
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setMinimumIdle(1);
            config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.pool.size")));
            config.setConnectionTimeout(Long.parseLong(properties.getProperty("db.connectionTimeout")));
            config.setIdleTimeout(Long.parseLong(properties.getProperty("db.idleTimeout")));
            config.setMaxLifetime(Long.parseLong(properties.getProperty("db.maxLifetime")));


            dataSource = new HikariDataSource(config);

        } catch (IOException e) {
            throw new RuntimeException("Error al cargar las propiedades de la base de datos", e);
        }
    }



    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}