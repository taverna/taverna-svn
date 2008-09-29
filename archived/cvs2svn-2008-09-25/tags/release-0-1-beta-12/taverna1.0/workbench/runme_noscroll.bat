@echo off

PATH=./bin/win32i386;%PATH%

set OPTS=-Xms256m -Xmx512m

set CP=.;./resources;./conf
for %%i in ("lib\*.jar") do call catenv.bat %%i
for %%i in ("plugins\*.jar") do call catenv.bat %%i

java %OPTS% -classpath %CP% -ea org.embl.ebi.escience.scuflui.workbench.Workbench