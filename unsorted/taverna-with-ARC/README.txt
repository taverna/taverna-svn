To build, run mvn install. If you have a newer maven version, you might need to modify
workbench-distro/src/main/assembly/developer-assembly.xml	workbench-distro/src/main/assembly/nightly-assembly.xml		workbench-distro/src/main/assembly/release-assembly.xml
to remove the finalName property line.
After mvn install, a fully runnable Taverna 2.1-SNAPSHOT should be present in workbench-distro/target
When running it, you will surely notice the new UseCase service provider which is being added by usecase-activity and usecase-activity-ui.
