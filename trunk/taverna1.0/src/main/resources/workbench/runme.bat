@echo off

set ARGS=-Xmx300m 
set ARGS=%ARGS% -Djava.system.class.loader=net.sf.taverna.tools.BootstrapClassLoader 
set ARGS=%ARGS% -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client 
set ARGS=%ARGS% "-Dtaverna.dotlocation=%~dp0\bin\win32i386\dot.exe"

REM uncomment the next line and complete to change the directory taverna downloads jars to
REM set ARGS=%ARGS% -Dtaverna.repository=<directory>

REM uncomment and complete the next line to set http proxy settings, and the 2nd line if authentication is required
REM set ARGS=%ARGS% -Dhttp.proxyHost=<hostname or ip address> -Dhttp.proxyPort=<port>
REM set ARGS=%ARGS% -Dhttp.proxyUser=<username> -Dhttp.proxyPassword=<password>

java %ARGS% -jar "%~dp0\taverna-bootstrap-1.5.0.jar" %*
