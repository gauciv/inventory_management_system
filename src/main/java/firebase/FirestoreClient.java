package firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.AccessToken;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Scanner;

public class FirestoreClient {
    private static final String BASE_URL = "https://firestore.googleapis.com/v1/projects/";
    private static String cachedToken = null;
    private static long tokenExpiryTime = 0;

    private static String getAccessToken() throws IOException {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return cachedToken;
        }
        try (InputStream serviceAccount = new FileInputStream("serviceAccountKey.json")) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/datastore"));
            credentials.refreshIfExpired();
            AccessToken token = credentials.getAccessToken();
            cachedToken = token.getTokenValue();
            tokenExpiryTime = token.getExpirationTime().getTime() - 60000;
            return cachedToken;
        }
    }

    public static String getDocument(String projectId, String documentPath, String unusedIdToken) throws Exception {
        String token = getAccessToken();
        String urlStr = BASE_URL + projectId + "/databases/(default)/documents/" + documentPath;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        return handleResponse(conn, "getDocument");
    }

    // --- FIX 1: Support PATCH via Method Override (For Updates) ---
    public static String setDocument(String projectId, String documentPath, String unusedIdToken, String jsonBody) throws Exception {
        String token = getAccessToken();
        String urlStr = BASE_URL + projectId + "/databases/(default)/documents/" + documentPath;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        // Java doesn't support "PATCH", so we use "POST" with an override header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }
        return handleResponse(conn, "setDocument");
    }

    // --- FIX 2: New Method for Creating Items (POST) ---
    public static String addDocument(String projectId, String collectionPath, String unusedIdToken, String jsonBody) throws Exception {
        String token = getAccessToken();
        String urlStr = BASE_URL + projectId + "/databases/(default)/documents/" + collectionPath;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        // Creating a new item uses standard POST
        conn.setRequestMethod("POST");
        
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }
        return handleResponse(conn, "addDocument");
    }

    public static void deleteDocument(String projectId, String collectionPath, String documentId, String unusedIdToken) throws Exception {
        String token = getAccessToken();
        String urlStr = BASE_URL + projectId + "/databases/(default)/documents/" + collectionPath + "/" + documentId;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        handleResponse(conn, "deleteDocument");
    }

    private static String handleResponse(HttpURLConnection conn, String operation) throws Exception {
        int responseCode = conn.getResponseCode();
        Scanner scanner = new Scanner(
            responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(), 
            "UTF-8"
        ).useDelimiter("\\A");
        String response = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception("Firestore " + operation + " failed (" + responseCode + "): " + response);
        }
        return response;
    }
}