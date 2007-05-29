@echo off
set ARGS=Xmx300m 
set ARGS=%ARGS% -Djava.system.class.loader=net.sf.taverna.tools.BootstrapClassLoader 
set ARGS=%ARGS% -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client 
set ARGS=%ARGS% -Draven.target.groupid=uk.org.mygrid.taverna.baclava
set ARGS=%ARGS% -Draven.target.artifactid=baclava-tools
set ARGS=%ARGS% -Draven.target.class=org.embl.ebi.escience.baclava.tools.DataThingViewer 
set ARGS=%ARGS% -Draven.target.method=main

java %ARGS% -jar "%~dp0\taverna-bootstrap-1.5.2-SNAPSHOT.jar" %*
