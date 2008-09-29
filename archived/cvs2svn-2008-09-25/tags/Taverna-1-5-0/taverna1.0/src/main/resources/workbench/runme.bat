@echo off

set ARGS=-Xmx300m 
set ARGS=%ARGS% -Djava.system.class.loader=net.sf.taverna.tools.BootstrapClassLoader 
set ARGS=%ARGS% -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client 
set ARGS=%ARGS% "-Dtaverna.dotlocation=%~dp0\bin\win32i386\dot.exe"

java %ARGS% -jar "%~dp0\taverna-bootstrap-1.5.0.jar" %*
