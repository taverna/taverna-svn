@echo off

set OPTS=-Xms256m -Xmx512m 
set OTPS=%OPTS% -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client
java %OPTS% -jar "%~dp0\lib\taverna-launcher-1.5-SNAPSHOT.jar" %*
