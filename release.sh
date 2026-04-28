#!/bin/bash

set -e

PS4="$(tput setaf 14)+ $(tput sgr0)"

echo

cat << 'EOF'
This script will perform the following actions:
 - Create a new release version.
 - Set the next development version.
 - Modify and commit the pom.xml file.
 - Push the changes to the remote repository.
 - Sign artifacts using the GPG key specified in pom.xml.
 - Publish artifacts to the Maven Central Repository.
EOF

log_i() {
    echo "$(tput setaf 2)$1$(tput sgr0)"
}

log_e() {
    echo "$(tput setaf 1)[ERROR] $1$(tput sgr0)"
}

DEFAULT_BRANCH="main"
if [ "$DEFAULT_BRANCH" != "$(git branch --show-current)" ]; then
    log_e "This script must be run on the '$DEFAULT_BRANCH' branch."
    exit 1
fi

if [ ! -f "./pom.xml" ]; then
    log_e "This script must be run from the project root directory."
    exit 1
fi

# Check if working tree is clean (no uncommitted changes)
if ! git diff-index --quiet HEAD --; then
    log_e "Working tree has uncommitted changes. Please commit or stash them before releasing."
    exit 1
fi

# Check if local main is up-to-date with remote main
log_i "Checking if local $DEFAULT_BRANCH is up-to-date with remote..."
REMOTE_COMMIT=$(git ls-remote origin "$DEFAULT_BRANCH" 2>/dev/null | awk '{print $1}')

if [ -z "$REMOTE_COMMIT" ]; then
    log_e "Remote branch '$DEFAULT_BRANCH' not found in origin. Check your remote configuration."
    exit 1
fi

LOCAL_COMMIT=$(git rev-parse "$DEFAULT_BRANCH")

if [ "$LOCAL_COMMIT" != "$REMOTE_COMMIT" ]; then
    # Check if local is behind remote (remote has commits local doesn't have)
    if git merge-base --is-ancestor "$LOCAL_COMMIT" "$REMOTE_COMMIT" 2>/dev/null; then
        log_e "Local '$DEFAULT_BRANCH' is behind remote. Please run 'git pull' first."
        exit 1
    # Check if local is ahead of remote (has unpushed commits)
    elif git merge-base --is-ancestor "$REMOTE_COMMIT" "$LOCAL_COMMIT" 2>/dev/null; then
        log_e "Local '$DEFAULT_BRANCH' has unpushed commits. Please push them first or reset to remote."
        exit 1
    else
        log_e "Local '$DEFAULT_BRANCH' has diverged from remote. Please synchronize with 'git pull --rebase'."
        exit 1
    fi
fi

echo

log_i "Verifying user and email for the commit"
echo -n 'user.name : '
git config user.name
echo -n 'user.email: '
git config user.email

echo
read -r -p "Is this information correct? Press Enter to continue, or Ctrl+C to abort"
echo

log_i "Verifying CHANGELOG.md"
read -r -p "Is the CHANGELOG.md up-to-date? Press Enter to continue, or Ctrl+C to abort"
echo

log_i "Current pom.xml and Java versions"
./mvnw -V help:evaluate -Dexpression=project.version -q -DforceStdout
echo
read -r -p "Is this information correct? Press Enter to continue, or Ctrl+C to abort"
echo

log_i "Enter credentials for deployment to Maven Central Repository (OSSRH)"
echo "(You can get these from https://central.sonatype.com/account)"
read -s -r -p "Username (OSSRH_USER): " OSSRH_USER
echo
read -s -r -p "Password (OSSRH_PASS): " OSSRH_PASS
echo
export OSSRH_USER
export OSSRH_PASS
echo

log_i "Enter GPG passphrase for key $(grep -oPm1 '(?<=<keyname>)[^<]+' pom.xml)"
read -s -r -p "GPG Passphrase: " MAVEN_GPG_PASSPHRASE
echo
export MAVEN_GPG_PASSPHRASE
echo

log_i "Generating release..."
set -x
./mvnw -V clean release:clean release:prepare release:perform --settings .mvn/settings.xml
set +x

echo

log_i "Showing the last 7 commits"
git log --pretty="%C(Yellow)%h  %C(reset)%ad (%C(Green)%cr%C(reset))%x09 %C(Cyan)%an (%ae): %C(reset)%s" -7

echo

log_i "Done."
