#!/bin/bash
#########################################################################################################
# Publishes the JavaDocs.
#
# Expects the following environment vars to be present:
# EXPECTED_REPO_SLUG    - Expected repository name.
# EXPECTED_BRANCH       - Expected branch name.
# JAVADOC_JDK           - JDK which to generate JavaDocs with.
# GH_TOKEN              - GitHub personal token.
#########################################################################################################

set -e

if [ "$TRAVIS_REPO_SLUG" != "$EXPECTED_REPO_SLUG" ]; then
    echo "Skipping snapshot deployment: wrong repository. Expected '$EXPECTED_REPO_SLUG' but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_JDK_VERSION" != "$JAVADOC_JDK" ]; then
    echo "Skipping snapshot deployment: wrong JDK. Expected '$JAVADOC_JDK' but was '$TRAVIS_JDK_VERSION'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    echo "Skipping snapshot deployment: this is a pull request."
elif [ "$TRAVIS_BRANCH" != "$EXPECTED_BRANCH" ]; then
    echo "Skipping snapshot deployment: wrong branch. Expected '$EXPECTED_BRANCH' but was '$TRAVIS_BRANCH'."
else
    echo "Publishing JavaDocs..."
    git config --global user.email "travis@travis-ci.org"
    git config --global user.name "travis-ci"
    chmod +x ./publish_javadoc.sh && ./publish_javadoc.sh ${EXPECTED_BRANCH} ${GH_TOKEN}
    echo "JavaDocs published!"
fi