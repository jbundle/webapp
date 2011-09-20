# 1. Commit current changes

# hg commit
# hg push

# 2. Create archetypes
---Ignore 'resource does not exist errors' on archetype:create-from-project---
cd website
mvn archetype:create-from-project

cd ../files
mvn archetype:create-from-project

cd ../webdav
mvn archetype:create-from-project

cd ../redirect
mvn archetype:create-from-project

cd ../proxy
mvn archetype:create-from-project

cd ../cgi
mvn archetype:create-from-project

cd ../upload
mvn archetype:create-from-project

cd ../webstart
mvn archetype:create-from-project

cd ..


# 4. Create and deploy archetype code
cd archetype
mvn install

cd target/archetype

-----------------NOTE:  I CHANGED the scm tag - FIX IN ARCHETYPE -------------
---MANUALLY CHANGE ALL POMS to have archetype-root as parent---
and remove:
  <distributionManagement>
    <status>generated</status>
  </distributionManagement>
---MANUALLY reinit remote repo-----
git init
cp ../../../.gitignore .
git add .
git commit -m "Initial commit"
git remote add origin git@github.com:jbundle/jbundle-webapp-archetype.git
git push origin master


# wait until after main code (#3) to do this:
mvn install
mvn release:clean
mvn release:prepare
mvn release:perform


# 3. Create and deploy servlet code
# do not do mvn clean
# or? mvn release:clean
mvn release:prepare

--IT FAILS: do:
mvn install

mvn release:prepare
mvn release:perform

# 5. Publish the new site

cd target/checkout/jbundle-util-webapp-site/
mvn clean
mvn site:site
mvn site:deploy

