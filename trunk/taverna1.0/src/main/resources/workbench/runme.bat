@echo off

set ARGS=Xmx300m 
set ARGS=%ARGS% -Djava.system.class.loader=net.sf.taverna.tools.BootstrapClassLoader 
set ARGS=%ARGS% -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client 

java %ARGS% -jar "%~dp0\lib\taverna-bootstrap-1.5-SNAPSHOT.jar" %*
