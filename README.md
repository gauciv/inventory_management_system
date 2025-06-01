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
docker compose up -d
```

3. Access the applications:
   - Main Application: http://localhost:8080
   - PHPMyAdmin: http://localhost:8081
   - Database port: 3306

## Development Setup

1. Clone the repository
2. Install dependencies:
```bash
mvn clean install
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

This project is licensed under the GNU Affero General Public License v3.0. See the [LICENSE](LICENSE) file for details.

## Support

For support, please contact the development team or open an issue in the repository.

## License

This project is licensed under the GNU Affero General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
