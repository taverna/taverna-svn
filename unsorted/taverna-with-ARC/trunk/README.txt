To build, run mvn install. If you have a newer maven version, you might need to modify

workbench-distro/src/main/assembly/developer-assembly.xml
workbench-distro/src/main/assembly/nightly-assembly.xml
workbench-distro/src/main/assembly/release-assembly.xml

to remove the finalName property line.

If a direct mvn install does not work, try:

cd plugins/activities-plugin/
#this will fail:
mvn clean install
cd ../..

cd usecase-activity
mvn clean install
cd ..

cd usecase-activity-ui
mvn clean install
cd ..

cd plugins/activities-plugin/
#should work now :)
mvn clean install
cd ../..

#and finally ..
mvn clean install


After mvn install, a fully runnable Taverna SNAPSHOT should be present in workbench-distro/target
When running it, you will surely notice the new UseCase service provider which is being added by usecase-activity and usecase-activity-ui.

If maven complains about missing POMs when trying to install, run mvn install inside the folder workbench-dist.
That will download the needed POMs, so afterwards you can run mvn install in the root folder.
