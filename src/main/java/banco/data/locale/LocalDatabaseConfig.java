package banco.data.locale;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LocalDatabaseConfig {

    private String url;
    private String username;
    private String password;

    public LocalDatabaseConfig(String propertiesFileName) throws IOException {
        Properties properties = new Properties();

        // Load the resource as a stream from the classpath
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFileName)) {
            if (input == null) {
                throw new IOException("Properties file not found: " + propertiesFileName);
            }

            properties.load(input);
            this.url = properties.getProperty("db.url");
            this.username = properties.getProperty("db.username");
            this.password = properties.getProperty("db.password");
        }
    }

    // Getters or other methods to use the properties
    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
