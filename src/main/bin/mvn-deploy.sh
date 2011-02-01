#!/bin/sh
BINDIR=`dirname $0`
ASADMIN=/usr/local/java/web/glassfish/bin/asadmin
WEB=/space/web

cd $BINDIR/../../..
mvn clean

mvn install

cd jbundle-util-webapp-site
mvn site
mvn site:deploy

cd ../jbundle-util-webapp-website
mvn glassfish:deploy

cd ../jbundle-util-webapp-files
mvn glassfish:deploy

cd ../jbundle-util-webapp-webdav
mvn glassfish:deploy

cd ../jbundle-util-webapp-upload
mvn glassfish:deploy

cd ../jbundle-util-webapp-redirect
mvn glassfish:deploy

cd ../jbundle-util-webapp-proxy
mvn glassfish:deploy

cd ../jbundle-util-webapp-cgi
mvn glassfish:deploy

cd ../jbundle-util-webapp-webstart/jbundle-util-webapp-webstart-jnlp
mvn glassfish:deploy
