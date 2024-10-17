package banco.data.remote;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class RemoteDatabaseManager {
    private static RemoteDatabaseManager instance;
    private final Logger logger = LoggerFactory.getLogger(RemoteDatabaseManager.class);
    private HikariDataSource dataSource;
    private boolean databaseInitTables = false; // Deberíamos inicializar las tablas? Fichero de configuración
    private String databaseUrl = "jdbc:postgresql://portgres:password@localhost:5432/cards"; // Fichero de configuración se lee en el constructor
    private String databaseInitScript = "bankcards/init.sql"; // Fichero de configuración se lee en el constructor
    private Connection conn;

    // Constructor privado para que no se pueda instanciar Singleton
    private RemoteDatabaseManager(Boolean forTesting) {
        if (!forTesting) {
            loadProperties();
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(databaseUrl);
            dataSource = new HikariDataSource(config);
            try (Connection conn = dataSource.getConnection()) {
                if (databaseInitTables) {
                    initTables(conn);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Método para obtener la instancia de la base de datos
     * Lo ideal e
     *
     * @return
     */
    public static synchronized RemoteDatabaseManager getInstance() {
        if (instance == null) {
            instance = new RemoteDatabaseManager(false);
        }
        return instance;
    }

    public static synchronized RemoteDatabaseManager getTestInstance(
            String url, String username, String password
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

    private synchronized void loadProperties() {
        logger.debug("Cargando fichero de configuración de la base de datos");
        try {
            var file = ClassLoader.getSystemResource("bankcards/application.properties").getFile();
            var props = new Properties();
            props.load(new FileReader(file));
            // Establecemos la url de la base de datos
            databaseUrl = props.getProperty("database.url");
            databaseInitTables = Boolean.parseBoolean(props.getProperty("database.initTables", "false"));
        } catch (IOException e) {
            logger.error("Error al leer el fichero de configuración de la base de datos " + e.getMessage());
        }
    }


    /**
     * Método para inicializar la base de datos y las tablas
     * Esto puede ser muy complejo y mejor usar un script, ademas podemos usar datos de ejemplo en el script
     */
    private synchronized void initTables(Connection conn) {
        try {
            executeScript(conn, databaseInitScript, true);
        } catch (FileNotFoundException e) {
            logger.error("Error al leer el fichero de inicialización de la base de datos " + e.getMessage());
        }
    }

    /**
     * Método para ejecutar un script de SQL
     *
     * @param conn
     * @param scriptSqlFile nombre del fichero de script SQL
     * @param logWriter     si queremos que nos muestre el log de la ejecución
     * @throws FileNotFoundException
     */
    public synchronized void executeScript(Connection conn, String scriptSqlFile, boolean logWriter) throws FileNotFoundException {
        ScriptRunner sr = new ScriptRunner(conn);
        var file = ClassLoader.getSystemResource(scriptSqlFile).getFile();
        logger.debug("Ejecutando script de SQL " + file);
        Reader reader = new BufferedReader(new FileReader(file));
        sr.setLogWriter(logWriter ? new PrintWriter(System.out) : null);
        sr.runScript(reader);
    }


    public synchronized Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}