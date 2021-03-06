#!/bin/bash
#########################################################################################################
# Deploys the artifacts.
#
# Expects the following environment vars to be present:
# EXPECTED_REPO_SLUG    - Expected repository name.
# EXPECTED_BRANCH       - Expected branch name.
# DEPLOY_JDK            - JDK for which to deploy.
# OSSRH_USERNAME        - Sonatype username.
# OSSRH_PASSWORD        - Sonatype password.
#########################################################################################################

set -e

if [ "$TRAVIS_REPO_SLUG" != "$EXPECTED_REPO_SLUG" ]; then
    echo "Skipping snapshot deployment: wrong repository. Expected '$EXPECTED_REPO_SLUG' but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_JDK_VERSION" != "$DEPLOY_JDK" ]; then
    echo "Skipping snapshot deployment: wrong JDK. Expected '$DEPLOY_JDK' but was '$TRAVIS_JDK_VERSION'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    echo "Skipping snapshot deployment: this is a pull request."
elif [ "$TRAVIS_BRANCH" != "$EXPECTED_BRANCH" ]; then
    echo "Skipping snapshot deployment: wrong branch. Expected '$EXPECTED_BRANCH' but was '$TRAVIS_BRANCH'."
else
    echo "Deploying snapshot..."
    mvn deploy --settings=.travis/settings.xml -Psnapshot -DskipTests
    echo "Snapshot deployed!"
fi