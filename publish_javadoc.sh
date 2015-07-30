#!/bin/bash
#########################################################################################################
# Publishes the JavaDocs to gh-pages.
#
# Usage: publish_javadoc.sh <dir_prefix> [<gh_token>]
#
# <dir_prefix> is appended to the "javadoc-" directory where the files will be copied.
# <gh_token> optional GitHub personal token to use. If not specified SSH access will be used.
#########################################################################################################

set -e

DIR_PREFIX=$1
[ "$DIR_PREFIX" == "" ] && echo "Please specify the DIR_PREFIX" && exit 1
GH_TOKEN=$2

GH_REPO=pingw33n/malle
JAVADOC_DIR=javadoc-${DIR_PREFIX}

if [ "$GH_TOKEN" == "" ]; then
    GH_URL=git@github.com:pingw33n/malle.git
else
    GH_URL=https://${GH_TOKEN}@github.com/${GH_REPO}
fi

mvn clean javadoc:aggregate -Pjavadoc
APIDOCS_DIR=`pwd`/target/site/apidocs
[ -d /tmp/gh-pages ] && rm -rf /tmp/gh-pages
git clone --branch=gh-pages ${GH_URL} /tmp/gh-pages
cd /tmp/gh-pages
[ -d "$JAVADOC_DIR" ] && git rm -rf ${JAVADOC_DIR} && rm -rf ${JAVADOC_DIR} > /dev/null
cp -r ${APIDOCS_DIR} ${JAVADOC_DIR}
git add -f ${JAVADOC_DIR}
git commit -m "Auto-publish JavaDoc" > /dev/null
git push -f origin gh-pages