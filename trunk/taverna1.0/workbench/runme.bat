@echo off

PATH=./bin/win32i386;%PATH%

set OPTS=-Xms256m -Xmx512m -Dtaverna.home=. -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client

set CLASSPATH=.;./resources;./conf;./taverna-launcher-1.3-SNAPSHOT.jar

for %%i in ("libext\*.jar") do call catenv.bat %%i

java %OPTS% -ea net.sf.taverna.tools.Launcher
