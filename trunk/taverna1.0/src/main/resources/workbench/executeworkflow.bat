@echo off
set ARGS=Xmx300m 
set ARGS=%ARGS% -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client 
set ARGS=%ARGS% -Djava.awt.headless=true
rem FIXME Avoid this hardcoded profile
set ARGS=%ARGS% -Draven.remoteprofile=file://%~dp0/.taverna/conf/default-profile.xml
set ARGS=%ARGS% -Draven.target.groupid=uk.org.mygrid.taverna.scufl
set ARGS=%ARGS% -Draven.target.artifactid=scufl-tools
set ARGS=%ARGS% -Draven.target.class=org.embl.ebi.escience.scufl.tools.WorkflowLauncher 
set ARGS=%ARGS% -Draven.target.method=main

java %ARGS% -jar "%~dp0\taverna-bootstrap-1.5-SNAPSHOT.jar" %*
