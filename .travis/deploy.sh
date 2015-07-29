#!/bin/bash

SLUG=pingw33n/malle
JDK=oraclejdk7
BRANCH=master

set -e

if [ "$TRAVIS_REPO_SLUG" != "$SLUG" ]; then
    echo "Skipping snapshot deployment: wrong repository. Expected '$SLUG' but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_JDK_VERSION" != "$JDK" ]; then
    echo "Skipping snapshot deployment: wrong JDK. Expected '$JDK' but was '$TRAVIS_JDK_VERSION'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    echo "Skipping snapshot deployment: this is a pull request."
elif [ "$TRAVIS_BRANCH" != "$BRANCH" ]; then
    echo "Skipping snapshot deployment: wrong branch. Expected '$BRANCH' but was '$TRAVIS_BRANCH'."
else
    echo "Deploying snapshot..."
    mvn clean deploy --settings=.travis/settings.xml -Psnapshot -DskipTests
    echo "Snapshot deployed!"
fi