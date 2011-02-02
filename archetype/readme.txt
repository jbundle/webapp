** See the bin/release.sh file **

  To create the archetypes:
  Go to each project in the root and type:
mvn archetype:create-from-project
  Then go to this project and type:
mvn install
  Then
cd target/archetype
mvn install

Make sure you are pointing to the correct archetype catalog:
mvn archetype:generate -DarchetypeGroupId=org.jbundle.util.webapp -DarchetypeArtifactId=jbundle-util-webapp-webstart-archetype -DarchetypeVersion=0.7.7-SNAPSHOT -DgroupId=org.jbundle.thin.base.screen -DartifactId=jbundle-calendarpanel-site -Dversion=0.7.7-SNAPSHOT -Dpackage=org.jbundle.thin.base.screen.calendar.site -DarchetypeRepository=file://home/don/workspace/jbundle-workspace/jbundle-util/jbundle-util-webapp-root/jbundle-util-webapp-site/src/site/resources/archetype-catalog.xml
  Done!
