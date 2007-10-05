@echo off

set ARGS=-Xmx300m 
set ARGS=%ARGS% -Djava.system.class.loader=net.sf.taverna.tools.BootstrapClassLoader 
set ARGS=%ARGS% -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client 
set ARGS=%ARGS% "-Dtaverna.dotlocation=%~dp0\bin\win32i386\dot.exe"

REM uncomment the next line and complete to change the directory taverna downloads jars to
REM set ARGS=%ARGS% -Dtaverna.repository=<directory>

REM NB: Proxy configuration settings have now been reverted to being in conf/mygrid.properties

java %ARGS% -jar "%~dp0\taverna-bootstrap-1.6.2.0.jar" %*
