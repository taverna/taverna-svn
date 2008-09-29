Building Taverna 1.5
====================

See the wiki page 
http://www.mygrid.org.uk/twiki/bin/view/Mygrid/BuildingTaverna
for updated information on how to build Taverna from the latest CVS
source code.

For the lazy, here's a quick summary:


1 	Install Java SDK 1.5
  	http://java.sun.com/j2se/1.5.0/download.jsp if not included with your
  	operating system

2 	Install CVS client (if not already installed)

3 	Install Maven 2.0.4 (the build system) 
  	http://maven.apache.org/download.html

4 	Fetch Taverna sourcecode from CVS 
  	cvs -z3 -d:pserver:anonymous@taverna.cvs.sourceforge.net:/cvsroot/taverna co -P taverna1.0
  	(Obviously you've already done that as you are reading this file)

5 	Build Taverna using Maven:
  	mvn package assembly:directory
  	('mvn package assembly:assembly' will create an assembled zip file).  	

6 	To run with the actual compiled components, rather than those fetched from the remote repository, first install the artifacts by running
	mvn install	
	Then edit the target/taverna-1.5.0-bin/taverna-1.5/runme.bat or target/taverna-1.5.0-bin/taverna-1.5.0/run.sh to add the argument -Draven.repository.0=file://path to local maven repository.
	In Windows this would involve adding a line similar to
	set ARGS=%ARGS% "-Draven.repository.0=file:///C:/Documents%20and%20Settings/name/.m2/repository/"
	Just before the java command. Likewise in Linux this would be
	ARGS="$ARGS -Draven.repository.0=file:///home/name/.m2/repository"
	
7 	Run Taverna from the new distribution directory  
	cd target/taverna-1.5.0-bin/taverna-1.5.0	
  	sh runme.sh or runme.bat

Note: Running on Linux requires that the 'graphviz' package is installed. This is available from http://www.graphviz.org/Download_linux.php

-- Stuart Owen, 2006-12-14
