package firebase;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FirebaseConfig {
    private static final String ENV_PATH = ".env";
    private static Properties props;

    static {
        props = new Properties();
        try (FileInputStream fis = new FileInputStream(ENV_PATH)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Could not load .env file: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static String getApiKey() {
        return get("FIREBASE_API_KEY");
    }
    public static String getProjectId() {
        return get("FIREBASE_PROJECT_ID");
    }
    public static String getAuthDomain() {
        return get("FIREBASE_AUTH_DOMAIN");
    }
    public static String getAppId() {
        return get("FIREBASE_APP_ID");
    }
    // Add more getters as needed
}
