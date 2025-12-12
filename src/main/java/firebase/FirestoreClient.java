package firebase;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class FirestoreClient {
    private static final String BASE_URL = "https://firestore.googleapis.com/v1/projects/";

    // Example: Get a document
    public static String getDocument(String projectId, String documentPath, String idToken) throws Exception {
        String urlStr = BASE_URL + projectId + "/databases/(default)/documents/" + documentPath;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + idToken);
        int responseCode = conn.getResponseCode();
        Scanner scanner = new Scanner(
            responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(), "UTF-8"
        ).useDelimiter("\\A");
        String response = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        if (responseCode != 200) {
            throw new Exception("Firestore getDocument failed: " + response);
        }
        return response;
    }

    // Example: Create or update a document
    public static String setDocument(String projectId, String documentPath, String idToken, String jsonBody) throws Exception {
        String urlStr = BASE_URL + projectId + "/databases/(default)/documents/" + documentPath;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Authorization", "Bearer " + idToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }
        int responseCode = conn.getResponseCode();
        Scanner scanner = new Scanner(
            responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(), "UTF-8"
        ).useDelimiter("\\A");
        String response = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        if (responseCode != 200) {
            throw new Exception("Firestore setDocument failed: " + response);
        }
        return response;
    }

    // NEW METHOD: Delete a document
    public static void deleteDocument(String projectId, String collectionPath, String documentId, String idToken) throws Exception {
        String urlStr = BASE_URL + projectId + "/databases/(default)/documents/" + collectionPath + "/" + documentId;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", "Bearer " + idToken);
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            Scanner scanner = new Scanner(conn.getErrorStream(), "UTF-8").useDelimiter("\\A");
            String response = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
            throw new Exception("Firestore deleteDocument failed: " + response);
        }
    }
}