@echo off

PATH=./bin/win32i386;%PATH%

set OPTS= -Xmx512m -Dtaverna.scrollDesktop

set CP=.;./resources;./conf
for %%i in ("lib\*.jar") do call catenv.bat %%i
for %%i in ("plugins\*.jar") do call catenv.bat %%i

java %OPTS% -classpath %CP% -ea org.embl.ebi.escience.scuflui.workbench.Workbench