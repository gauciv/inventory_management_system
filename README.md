# Inventory Management System

## Prerequisites
- Docker Desktop
  - For Windows: Install Docker Desktop and WSL2
  - For Linux: Install Docker and Docker Compose
- Git

## Setup Instructions

1. Clone the repository:
```bash
git clone [your-repository-url]
cd inventory_management_system
```

2. Configure environment (optional):
   - Copy `.env.example` to `.env`
   - Modify the ports in `.env` if needed (if you have conflicts)

3. Start the application:
```bash
docker compose up -d
```

4. Access the applications:
   - PHPMyAdmin: http://localhost:8080
   - Database port: 3306 (configurable in .env)

## Troubleshooting

### Port Conflicts
If you get port conflicts (e.g., port 3306 or 8080 is already in use):
1. Stop your local MySQL if it's running
2. OR modify the ports in the `.env` file

### Windows-Specific Issues
1. Make sure Docker Desktop is running
2. Ensure WSL2 is installed and configured
3. If you get line ending errors, run:
   ```bash
   git config --global core.autocrlf true
   ```
   Then clone the repository again

### Permission Issues
If you get permission issues with mvnw:
```bash
git update-index --chmod=+x mvnw
```
