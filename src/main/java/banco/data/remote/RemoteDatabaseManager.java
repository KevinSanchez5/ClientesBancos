package banco.data.remote;

import banco.Main;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class RemoteDatabaseManager {
    private static RemoteDatabaseManager instance;
    private final Logger logger = LoggerFactory.getLogger(RemoteDatabaseManager.class);
    private HikariDataSource dataSource;
    private String databaseUrl = "jdbc:postgresql://postgres-db:5432/cards"; // Fichero de configuración se lee en el constructor
    private Connection conn;
    private String username;
    private String password;
    private Integer poolSize;

    /** 
     * Constructor privado para que no se pueda instanciar Singleton
     * @param forTesting decide si se inicia el dataSource
     */
    private RemoteDatabaseManager(Boolean forTesting) {
        if (!forTesting) {
            HikariConfig config = new HikariConfig();
            getProperties(); // Leemos la url de la base de datos desde el fichero de propiedades
            config.setJdbcUrl(databaseUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(poolSize);
            config.setDriverClassName("org.postgresql.Driver");
            dataSource = new HikariDataSource(config);
        }
    }

    /**
     * Método para obtener la instancia del manager de la base de datos remota
     * @return una instancia de RemoteDatabaseManager
     */
    public static synchronized RemoteDatabaseManager getInstance() {
        if (instance == null) {
            instance = new RemoteDatabaseManager(false);
        }
        return instance;
    }

    private void getProperties(){
        logger.debug("Cargando fichero de configuración de la base de datos");
        try {
            var file = Main.class.getClassLoader().getResourceAsStream("bankcards/database.properties");
            var props = new Properties();
            props.load(file);
            // Establecemos la url de la base de datos
            databaseUrl = props.getProperty("cards.database.url", "jdbc:postgresql://postgres-db:5432/cards");
            username = props.getProperty("cards.database.username", "postgres");
            password = props.getProperty("cards.database.password", "password");
            poolSize = Integer.parseInt(props.getProperty("cards.database.pool.size", "10"));

        } catch (IOException e) {
            logger.error("Error al leer el fichero de configuración de la base de datos " + e.getMessage());
        }

    }

    /**
     * Método para obtener la instancia del manager de la base de datos remota dedicado para tests
     * @return una instancia de RemoteDatabaseManager que no tiene creada la conexion
     */
    public static synchronized RemoteDatabaseManager getTestInstance(
            String url, 
            String username, 
            String password
    ) {
        if (instance == null) {
            instance = new RemoteDatabaseManager(true);
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            instance.dataSource = new HikariDataSource(config);
        }
        return instance;
    }
    
    /**
     * Obtiene una conexión a la base de datos remota.
     *
     * Este método se encarga de obtener una conexión a la base de datos remota utilizando el
     * {@link HikariDataSource} que se configura en el constructor de la clase. La conexión
     * se obtiene de manera sincronizada para evitar problemas de concurrencia.
     *
     * @return Un objeto {@link Connection} que representa la conexión a la base de datos.
     * @throws SQLException Si se produce algún error durante la obtención de la conexión.
     */
    public synchronized Connection getConnection() throws SQLException {
        logger.debug("Obteniendo una conexión a la base de datos");
        return dataSource.getConnection();
    }
}