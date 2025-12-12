package firebase;

import org.json.JSONObject;
import java.io.File;
import java.nio.file.Files;

public class FirebaseConfig {
    
    private static String projectId = null;

    /**
     * Reads the Project ID from the serviceAccountKey.json file.
     * This ensures it works without an .env file.
     */
    public static String getProjectId() {
        // Return cached value if we already found it
        if (projectId != null) {
            return projectId;
        }

        try {
            File keyFile = new File("serviceAccountKey.json");
            
            if (!keyFile.exists()) {
                System.err.println("CRITICAL ERROR: serviceAccountKey.json not found in current directory.");
                return null;
            }

            // Read the file content
            String content = new String(Files.readAllBytes(keyFile.toPath()));
            JSONObject json = new JSONObject(content);
            
            // Extract the project_id field
            if (json.has("project_id")) {
                projectId = json.getString("project_id");
                System.out.println("Loaded Project ID from file: " + projectId);
            } else {
                System.err.println("Error: serviceAccountKey.json does not contain 'project_id'");
            }

        } catch (Exception e) {
            System.err.println("Failed to read Project ID from key file: " + e.getMessage());
            e.printStackTrace();
        }
        
        return projectId;
    }

    // This is no longer used by FirebaseAuth (we hardcoded it there), 
    // but kept here to prevent compilation errors if other files reference it.
    public static String getApiKey() {
        return ""; 
    }
}