#!/bin/sh
BINDIR=`dirname $0`
cd $BINDIR/../../..

mvn clean
mvn install

cd jbundle-util-webapp-website
mvn archetype:create-from-project

cd ../jbundle-util-webapp-files
mvn archetype:create-from-project

cd ../jbundle-util-webapp-webdav
mvn archetype:create-from-project

cd ../jbundle-util-webapp-upload
mvn archetype:create-from-project

cd ../jbundle-util-webapp-redirect
mvn archetype:create-from-project

cd ../jbundle-util-webapp-proxy
mvn archetype:create-from-project

cd ../jbundle-util-webapp-cgi
mvn archetype:create-from-project

cd ../jbundle-util-webapp-webstart
mvn archetype:create-from-project

cd ../jbundle-util-webapp-archetype
mvn clean package

# NOTE: Here you will have to edit all the archetypes poms to have jbundle-util-webapp-archetype-root as their parent
# (So they will do the signing)

cd target/archetype/
# mvn clean package

# mvn deploy -DperformRelease=true -Dgpg.passphrase=xyz123
