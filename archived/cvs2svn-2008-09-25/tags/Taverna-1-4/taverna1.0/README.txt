Building Taverna
================

See the wiki page 
http://www.mygrid.org.uk/twiki/bin/view/Mygrid/BuildingTaverna
for updated information on how to build Taverna from the latest CVS
source code.

For the lazy, here's a quick summary:


1 Install Java SDK 1.5
  http://java.sun.com/j2se/1.5.0/download.jsp if not included with your
  operating system

2 Install CVS client (if not already installed)

3 Install Maven 2 (the build system) 
  http://maven.apache.org/download.html

4 Fetch Taverna sourcecode from CVS 
  cvs -z3 -d:pserver:anonymous@taverna.cvs.sourceforge.net:/cvsroot/taverna co -P taverna1.0
  (Obviously you've already done that as you are reading this file)

5 Build Taverna using Maven:
  mvn -f parent-pom.xml package assembly:directory

6 Run Taverna from the new distribution directory
  cd target/taverna-1.4-bin/taverna-1.4
  sh runme.sh


-- Stian Soiland, 2006-05-30
