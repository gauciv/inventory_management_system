# Inventory Management System

A modern, Java-based inventory management system with a JavaFX GUI interface. This system helps businesses track and manage their inventory efficiently.

## Features

- User authentication and role-based access control
- Real-time inventory tracking
- Product management (add, edit, delete)
- Stock level monitoring
- Transaction history
- Database backup and restore
- Modern and intuitive user interface

## Technology Stack

- **Backend**: Java 24
- **GUI**: JavaFX 24.0.1
- **Database**: MySQL
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose
- **Database Management**: PHPMyAdmin

## Prerequisites

- Docker Desktop
  - For Windows: Install Docker Desktop and WSL2
  - For Linux: Install Docker and Docker Compose
- Git
- Java 24 or later (for local development)
- Maven (for local development)

## Quick Start

1. Clone the repository:
```bash
git clone [your-repository-url]
cd inventory_management_system
```

2. Start the application using Docker:
```bash

## Development Setup

```

3. Run the application:
```bash
mvn javafx:run
```

## Project Structure

```
inventory_management_system/
├── src/                    # Source code
│   ├── main/
│   │   ├── java/          # Java source files
│   │   └── resources/     # Application resources
├── deployment/            # Deployment configurations
├── lib/                   # External libraries
├── docker-compose.yml    # Docker Compose configuration
├── Dockerfile           # Docker configuration
└── pom.xml              # Maven configuration
```

## Database

The system uses MySQL as its database. The database schema is automatically created when the application starts for the first time. You can manage the database through PHPMyAdmin at http://localhost:8081.

## Firebase Integration Setup

This project is now prepared for Firebase integration. All MySQL/JDBC/database_utility code has been removed and replaced with clear TODOs for Firebase in all controllers. Please follow these steps to complete the setup:

### 1. Configure Firebase Environment Variables

Copy `.env.example` to `.env` and fill in your Firebase project details:

```
cp .env.example .env
```

Edit `.env` and provide your Firebase credentials:

- FIREBASE_API_KEY
- FIREBASE_AUTH_DOMAIN
- FIREBASE_PROJECT_ID
- FIREBASE_STORAGE_BUCKET
- FIREBASE_MESSAGING_SENDER_ID
- FIREBASE_APP_ID
- FIREBASE_MEASUREMENT_ID

### 2. Implement Firebase Logic

All previous database logic in the following controllers has been replaced with `// TODO` comments for Firebase:
- add_stocks/addproductController.java
- add_stocks/addstocksController.java
- add_edit_product/addeditproductController.java
- dashboard/dashboardController.java
- dashboard/SalesController.java
- forecasting/ForecastingController.java
- sold_stocks/soldstocksController.java
- login/login_controller.java

Replace each TODO with the appropriate Firebase SDK calls for data access, updates, and queries.

### 3. Removed Files and Configs

- All MySQL, JDBC, and database_utility code
- All SQL schema and database files
- All Docker Compose and environment configs for MySQL/PHPMyAdmin

### 4. Next Steps

- Add your Firebase credentials to `.env`
- Implement Firebase logic in all marked TODOs
- Test the application thoroughly

---

For any questions or migration details, see the code comments and the file `addproductController.DBREMOVED.txt` for a summary of removed logic.

## Firebase REST API Usage in Java

This project uses the Firebase REST API for authentication and Firestore access, as there is no official Java SDK for desktop apps.

### 1. Authentication
- Use `FirebaseAuth.signInWithEmailPassword(email, password)` to authenticate users.
- The returned JSON contains `idToken`, which is required for Firestore requests.

### 2. Firestore Access
- Use `FirestoreClient.getDocument(projectId, documentPath, idToken)` to fetch a document.
- Use `FirestoreClient.setDocument(projectId, documentPath, idToken, jsonBody)` to create/update a document.
- All requests require a valid `idToken` from authentication.

### 3. Configuration
- All Firebase credentials are loaded from `.env` using `FirebaseConfig`.

See the `firebase/` package for helper classes and usage examples.

## Troubleshooting

### Port Conflicts
If you encounter port conflicts (e.g., port 3306 or 8080 is already in use):
1. Stop your local MySQL if it's running
2. OR modify the ports in the `.env` file

### Windows-Specific Issues
1. Ensure Docker Desktop is running
2. Verify WSL2 is installed and configured
3. If you get line ending errors, run:
   ```bash
   git config --global core.autocrlf true
   ```
   Then clone the repository again

### Permission Issues
If you encounter permission issues with mvnw:
```bash
git update-index --chmod=+x mvnw
```

## Contributing

## Support

For support, please contact the development team or open an issue in the repository.

## License

This project is licensed under the GNU Affero General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
