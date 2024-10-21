package banco.data.local;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LocalDatabaseConfig {
    private String url;
    private String username;
    private String password;

    /**
     * Constructor lee las propiedades de un archivo de configuración
     * @param propertiesFilePath
     * @throws IOException
     */
    public LocalDatabaseConfig(String propertiesFilePath) throws IOException {
        Properties properties = new Properties();
        try (InputStream input =getClass().getClassLoader().getResourceAsStream(propertiesFilePath)) {
            properties.load(input);
            this.url = properties.getProperty("db.url");
            this.username = properties.getProperty("db.username");
            this.password = properties.getProperty("db.password");
        }
    }
    public String getUsername() {
        return username;
    }
}
