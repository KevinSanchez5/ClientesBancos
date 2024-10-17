package banco.data.local;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LocalDatabaseConfig {
    private String url;
    private String username;
    private String password;

    public LocalDatabaseConfig(String propertiesFilePath) throws IOException {
        Properties properties = new Properties();
        try (InputStream input =getClass().getClassLoader().getResourceAsStream(propertiesFilePath)) {
            properties.load(input);
            this.url = properties.getProperty("db.url");
            this.username = properties.getProperty("db.username");
            this.password = properties.getProperty("db.password");
        }
    }

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
