@echo off
set MAIN=org.embl.ebi.escience.baclava.tools.DataThingViewer
set OPTS=-Xms256m -Xmx512m 
set OPTS=%OPTS% -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client 
set OPTS=%OPTS% -Dtaverna.main=%MAIN%
set OPTS=%OPTS% -Dtaverna.scrollDesktop
java %OPTS% -jar ./taverna-launcher-1.3-SNAPSHOT.jar %*
