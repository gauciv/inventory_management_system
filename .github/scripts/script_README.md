# Gitflow Automation Script

This script automates common Git operations such as fetching, pulling, branching, committing, and pushing changes. Follow the instructions below to set up and run the script on your system.

---

## Prerequisites

1. **Python**: Ensure Python 3.6 or higher is installed.
   - **Linux**: Run `python3 --version` to check.
   - **Windows**: Run `python --version` in Command Prompt or PowerShell.

2. **Git**: Ensure Git is installed and available in your system's PATH.
   - **Linux**: Run `git --version` to check.
   - **Windows**: Run `git --version` in Command Prompt or PowerShell.

3. **Dependencies**: Install the required Python packages in a virtual environment to avoid conflicts:
   - Create a virtual environment:
     ```bash
     python3 -m venv venv
     ```
     (On Windows, use `python -m venv venv`.)
   - Activate the virtual environment:
     - **Linux**:
       ```bash
       source venv/bin/activate
       ```
     - **Windows**:
       ```cmd
       venv\Scripts\activate
       ```
   - Install dependencies:
     ```bash
     pip install gitpython
     ```

---

## Setup Instructions

### Linux

1. Make the script executable:
   ```bash
   chmod +x .github/workflows/git-script.py
   ```

2. Ensure the script is in a Git repository. Navigate to the root of your Git project.

---

### Windows

1. Ensure the script is in a Git repository. Navigate to the root of your Git project.

2. Use the script with Python directly (no need to make it executable).

---

## Running the Script

### Linux

Run the script using:
```bash
./.github/workflows/git-script.py
```

You can also pass optional arguments:
- `--branch` or `-b`: Specify the branch name to create/use.
- `--message` or `-m`: Specify the commit message.

Example:
```bash
./.github/workflows/git-script.py -b feature/new-feature -m "Initial commit"
```

---

### Windows

Run the script using:
```cmd
python .github/workflows/git-script.py
```

You can also pass optional arguments:
- `--branch` or `-b`: Specify the branch name to create/use.
- `--message` or `-m`: Specify the commit message.

Example:
```cmd
python .github/workflows/git-script.py -b feature/new-feature -m "Initial commit"
```

---

## How the Script Handles Git Flow

1. **Fetch and Pull**:
   - Fetches changes from the remote repository.
   - Checks for differences between the local and remote branches.
   - If local changes are detected, stashes them before pulling updates and restores them afterward.

2. **Branch Management**:
   - Validates branch names to ensure they follow Git naming conventions.
   - Creates a new branch if it doesn't exist or switches to an existing branch.

3. **Commit Workflow**:
   - Stages all changes in the repository.
   - Prompts for a commit message if not provided as an argument.
   - Confirms the commit message before proceeding.

4. **Push Changes**:
   - Pushes the committed changes to the remote repository.
   - Handles errors like authentication issues or conflicts during the push.

5. **Interactive and User-Friendly**:
   - Uses color-coded messages to indicate success, warnings, and errors.
   - Provides detailed logs for each step of the process.

---

## Notes

- Ensure you have the necessary permissions to push changes to the remote repository.
- If you encounter any issues, check the error messages displayed by the script for guidance.