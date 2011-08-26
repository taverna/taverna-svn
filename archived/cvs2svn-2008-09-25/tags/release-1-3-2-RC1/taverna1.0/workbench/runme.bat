@echo off

PATH=./bin/win32i386;%PATH%

set OPTS=-Xms256m -Xmx512m -Dtaverna.home=. -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client

set CLASSPATH=.;./resources;./conf;./taverna-1.3.2-RC1-launcher.jar

for %%i in ("libext\*.jar") do call catenv.bat %%i

java %OPTS% -ea org.embl.ebi.escience.scuflui.workbench.WorkbenchLauncher
