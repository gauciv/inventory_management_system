package firebase;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class FirebaseAuth {
    // Sign in with email and password using Firebase Auth REST API
    public static String signInWithEmailPassword(String email, String password) throws Exception {
        String apiKey = FirebaseConfig.getApiKey();
        String endpoint = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;
        String payload = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}", email, password);

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }
        int responseCode = conn.getResponseCode();
        Scanner scanner = new Scanner(
            responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(), "UTF-8"
        ).useDelimiter("\\A");
        String response = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        if (responseCode != 200) {
            throw new Exception("Firebase Auth failed: " + response);
        }
        // The response contains idToken, refreshToken, etc.
        return response;
    }
}
