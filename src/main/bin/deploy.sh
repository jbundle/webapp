#!/bin/sh
BINDIR=`dirname $0`
ASADMIN=/usr/local/java/web/glassfish/bin/asadmin
WEB=/space/web
VERSION=0.7.6-SNAPSHOT
DEPLOY_COMMAND=redeploy
HOST=www.tourgeek.com
USER=admin
PASSWORDFILE=/home/don/.asadminpassword

cd $BINDIR/../../..
mvn clean

mvn install

PROJECT=jbundle-util-webapp-website
CONTEXT=/util/webapp/$PROJECT
$ASADMIN --user=$USER --host=$HOST --passwordfile=$PASSWORDFILE $DEPLOY_COMMAND --virtualservers jbundle --name $PROJECT --contextroot $CONTEXT ./$PROJECT/target/$PROJECT-$VERSION.war

PROJECT=jbundle-util-webapp-files
CONTEXT=/util/webapp/$PROJECT
$ASADMIN --user=$USER --host=$HOST --passwordfile=$PASSWORDFILE $DEPLOY_COMMAND --virtualservers jbundle --name $PROJECT --contextroot $CONTEXT ./$PROJECT/target/$PROJECT-$VERSION.war

PROJECT=jbundle-util-webapp-webdav
CONTEXT=/util/webapp/$PROJECT
$ASADMIN --user=$USER --host=$HOST --passwordfile=$PASSWORDFILE $DEPLOY_COMMAND --virtualservers jbundle --name $PROJECT --contextroot $CONTEXT ./$PROJECT/target/$PROJECT-$VERSION.war

PROJECT=jbundle-util-webapp-upload
CONTEXT=/util/webapp/$PROJECT
$ASADMIN --user=$USER --host=$HOST --passwordfile=$PASSWORDFILE $DEPLOY_COMMAND --virtualservers jbundle --name $PROJECT --contextroot $CONTEXT ./$PROJECT/target/$PROJECT-$VERSION.war

PROJECT=jbundle-util-webapp-redirect
CONTEXT=/util/webapp/$PROJECT
$ASADMIN --user=$USER --host=$HOST --passwordfile=$PASSWORDFILE $DEPLOY_COMMAND --virtualservers jbundle --name $PROJECT --contextroot $CONTEXT ./$PROJECT/target/$PROJECT-$VERSION.war

PROJECT=jbundle-util-webapp-proxy
CONTEXT=/util/webapp/$PROJECT
$ASADMIN --user=$USER --host=$HOST --passwordfile=$PASSWORDFILE $DEPLOY_COMMAND --virtualservers jbundle --name $PROJECT --contextroot $CONTEXT ./$PROJECT/target/$PROJECT-$VERSION.war

PROJECT=jbundle-util-webapp-cgi
CONTEXT=/util/webapp/$PROJECT
$ASADMIN --user=$USER --host=$HOST --passwordfile=$PASSWORDFILE $DEPLOY_COMMAND --virtualservers jbundle --name $PROJECT --contextroot $CONTEXT ./$PROJECT/target/$PROJECT-$VERSION.war

PROJECT=jbundle-util-webapp-webstart-jnlp
CONTEXT=/util/webapp/$PROJECT
$ASADMIN --user=$USER --host=$HOST --passwordfile=$PASSWORDFILE $DEPLOY_COMMAND --virtualservers jbundle --name $PROJECT --contextroot $CONTEXT ./jbundle-util-webapp-webstart/$PROJECT/target/$PROJECT-$VERSION.war
