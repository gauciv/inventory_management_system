# DARA MGA TAPULAN 
#!/usr/bin/env python3
import os
import re
import platform
import sys
import argparse
from git import Repo, GitCommandError

# ANSI color codes
GREEN = '\033[92m'
YELLOW = '\033[93m'
RED = '\033[91m'
CYAN = '\033[96m'
RESET = '\033[0m'
BLUE = '\033[94m'
WHITE_BG = '\033[107m'
BLACK_TEXT = '\033[30m'
MAGENTA = '\033[95m'
INPUT_BG = '\033[100m'  # Gray Background
INPUT_TEXT = '\033[97m'  # White Text

# ----------------------------------------
# Utility Functions
# ----------------------------------------
def print_banner(title: str):
    print(f"{RESET}\n{WHITE_BG}{BLACK_TEXT}{' ' * 5} {title.center(40)} {' ' * 5}{RESET}")

def print_section(title: str):
    print(f"\n{BLUE}>> [PROCESS] {title.upper()} {RESET}")

def print_log(message: str, color: str = RESET):
    print(f"{color}>> {message}{RESET}")

# ----------------------------------------
# Validators & Prompts
# ----------------------------------------
def valid_branch(name: str) -> bool:
    return bool(re.match(r'^(?!/|.*([~^:\?\*\[\\])).+(?<!/)$', name))

def prompt_branch(repo, arg_branch=None):
    print_section("Branch Setup")
    if arg_branch:
        if not valid_branch(arg_branch):
            print_log(f"[ERROR] Invalid branch name: {arg_branch}", RED)
            sys.exit(1)
        return arg_branch
    while True:
        name = input(f"{INPUT_BG}{INPUT_TEXT} + ENTER BRANCH NAME: {RESET} ").strip()
        if not valid_branch(name):
            print_log("[WARNING] Illegal branch name. Avoid spaces or ~ ^ : ? * [ \\ and no leading/trailing '/'.", YELLOW)
            continue
        return name

def prompt_commit_msg(arg_msg=None):
    print_section("Commit Setup")
    if arg_msg:
        return arg_msg
    while True:
        msg = input(f"{INPUT_BG}{INPUT_TEXT} + ENTER COMMIT MESSAGE: {RESET} ").strip()
        if not msg:
            print_log("[WARNING] Commit message cannot be empty.", YELLOW)
            continue
        confirm = input(f"{INPUT_BG}{INPUT_TEXT} ? CONFIRM MESSAGE [Y/N]: {RESET} ").lower()
        if confirm.startswith('y'):
            return msg

def clear_screen():
    if platform.system() == "Windows":
        os.system("cls")
    else:
        os.system("clear")

# ----------------------------------------
# Git Operations
# ----------------------------------------
def fetch_and_pull(repo):
    print_section("Fetching Remote Changes")
    try:
        origin = repo.remote('origin')
        origin.fetch()
        print_log("[SUCCESS] Fetched latest changes from remote.", GREEN)

        remote_diff = repo.git.log('HEAD..origin/main', '--oneline')
        if remote_diff:
            print_log("[INFO] Changes detected on the remote repository.", MAGENTA)
            if repo.is_dirty(untracked_files=True):
                print_log("[WARNING] Local changes detected. Stashing changes before pulling.", YELLOW)
                repo.git.stash('save', '--include-untracked')
                repo.git.pull('origin', 'main')
                print_log("[SUCCESS] Pulled latest changes into 'main'.", GREEN)
                if repo.git.stash('list'):
                    print_log("[INFO] Restoring stashed changes.", MAGENTA)
        else:
            if not repo.is_dirty(untracked_files=True):
                print_log("[NOTICE] No changes on the remote and the working tree is clean.", YELLOW)
                print_log("[INFO] Please make some changes before running the script again.", MAGENTA)
                sys.exit(0)
            else:
                print_log("[INFO] No changes detected on the remote. Proceeding with the script.", MAGENTA)
    except GitCommandError as e:
        print_log(f"[ERROR] Fetch/Pull failed: {e}", RED)
        sys.exit(1)

# ----------------------------------------
# Main Workflow
# ----------------------------------------
def main():
    clear_screen()
    print_banner("GITFLOW AUTOMATION")
    parser = argparse.ArgumentParser(
        description=f"{CYAN}=== Gitflow Automation ==={RESET}",
        formatter_class=argparse.RawTextHelpFormatter
    )
    parser.add_argument("--branch", "-b", help="Branch name to create/use")
    parser.add_argument("--message", "-m", help="Commit message to use")
    args = parser.parse_args()

    try:
        repo = Repo('.', search_parent_directories=True)
    except Exception:
        print_log("[ERROR] Not a git repository.", RED)
        sys.exit(1)

    fetch_and_pull(repo)
    branch = prompt_branch(repo, args.branch)
    try:
        repo.git.checkout('-b', branch)
        print_log(f"[SUCCESS] Switched to branch: {branch}", GREEN)
    except GitCommandError:
        try:
            repo.git.checkout(branch)
            print_log(f"[SUCCESS] Checked out existing branch: {branch}", GREEN)
        except GitCommandError as e:
            print_log(f"[ERROR] Could not checkout branch: {e}", RED)
            sys.exit(1)

    try:
        repo.git.add('--all')
        print_section("Staging Changes")
        print_log("[SUCCESS] Staged all changes", GREEN)
    except GitCommandError as e:
        print_log(f"[ERROR] Staging failed: {e}", RED)
        sys.exit(1)

    msg = prompt_commit_msg(args.message)
    try:
        repo.git.commit('-m', msg)
        print_log(f"[SUCCESS] Committed: '{msg}'", GREEN)
    except GitCommandError as e:
        print_log(f"[ERROR] Commit failed: {e}", RED)
        sys.exit(1)

    try:
        print_section("Pushing Changes")
        origin = repo.remote('origin')
        origin.push(branch)
        print_log(f"[SUCCESS] Pushed '{branch}' to origin", GREEN)
    except GitCommandError as e:
        print_log(f"[ERROR] Push failed: {e}", RED)
        sys.exit(1)

    print_banner("GITFLOW COMPLETE")

if __name__ == "__main__":
    main()
