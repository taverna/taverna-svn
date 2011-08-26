@echo off
set MAIN=org.embl.ebi.escience.scufl.tools.WorkflowLauncher
set OPTS=-Xms256m -Xmx512m 
set OPTS=%OPTS% -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client 
set OPTS=%OPTS% -Djava.awt.headless=true
set OPTS=%OPTS% -Dtaverna.main=%MAIN%
java %OPTS% -jar "%~dp0\taverna-launcher-1.4.jar" %*
