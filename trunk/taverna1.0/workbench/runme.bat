@echo off

PATH=./bin/win32i386;%PATH%

set OPTS= -Xmx512m -Dtaverna.scrollDesktop -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client -Dtaverna.home=.

set CLASSPATH=.;./resources;./conf
for %%i in ("lib\*.jar") do call catenv.bat %%i
for %%i in ("plugins\*.jar") do call catenv.bat %%i

java %OPTS% -ea org.embl.ebi.escience.scuflui.workbench.Workbench