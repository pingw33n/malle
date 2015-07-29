#!/bin/bash

set -e

DIR_PREFIX=$1
[ "$DIR_PREFIX" == "" ] && echo "Please specify the DIR_PREFIX" && exit 1
GH_TOKEN=$2

GH_REPO=pingw33n/malle
JAVADOC_DIR=javadoc-$DIR_PREFIX

if [ "$GH_TOKEN" == "" ]; then
    GH_URL=git@github.com:pingw33n/malle.git
else
    GH_URL=https://$GH_TOKEN@github.com/$GH_REPO
fi

mvn clean javadoc:aggregate -Pjavadoc
APIDOCS_DIR=`pwd`/target/site/apidocs
[ -d /tmp/gh-pages ] && rm -rf /tmp/gh-pages
git clone --branch=gh-pages $GH_URL /tmp/gh-pages
cd /tmp/gh-pages
[ -d "$JAVADOC_DIR" ] && git rm -rf $JAVADOC_DIR && rm -rf $JAVADOC_DIR > /dev/null
cp -r $APIDOCS_DIR $JAVADOC_DIR
git add -f $JAVADOC_DIR
git commit -m "auto-publish javadoc" > /dev/null
git push -f origin gh-pages