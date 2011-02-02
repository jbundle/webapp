# 1. Commit current changes

# hg commit
# hg push

# 2. Create archetypes
---Ignore 'resource does not exist errors' on archetype:create-from-project---
cd jbundle-util-webapp-website
mvn archetype:create-from-project

cd ../jbundle-util-webapp-files
mvn archetype:create-from-project

cd ../jbundle-util-webapp-webdav
mvn archetype:create-from-project

cd ../jbundle-util-webapp-redirect
mvn archetype:create-from-project

cd ../jbundle-util-webapp-proxy
mvn archetype:create-from-project

cd ../jbundle-util-webapp-cgi
mvn archetype:create-from-project

cd ../jbundle-util-webapp-upload
mvn archetype:create-from-project

cd ../jbundle-util-webapp-webstart
mvn archetype:create-from-project

cd ..


--------- try doing step 4 first-----------

# 3. Create and deploy servlet code
# do not do mvn clean
# or? mvn release:clean
mvn release:prepare

--IT FAILS: do:
mvn install

mvn release:prepare
mvn release:perform

# 4. Create and deploy archetype code
cd jbundle-util-webapp-archetype
mvn install

cd target/archetype

-----------------NOTE:  I CHANGED the scm tag - FIX IN ARCHETYPE -------------
---MANUALLY CHANGE ALL POMS to have archetype-root as parent---
and remove:
  <distributionManagement>
    <status>generated</status>
  </distributionManagement>
---MANUALLY reinit remote repo-----
ssh donandann.com
cd /space/hg/jbundle-util-webapp-archetype-root
rm -fr .hg
hg init
exit
--------------

hg init
hg add
hg commit -m "Initial test commit"
hg push ssh://www.donandann.com//space/hg/jbundle-util-webapp-archetype-root

mvn install
mvn release:clean
mvn release:prepare
mvn release:perform

# 5. Publish the new site

cd target/checkout/jbundle-util-webapp-site/
mvn clean
mvn site:site
mvn site:deploy
