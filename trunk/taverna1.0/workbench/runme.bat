@echo off

PATH=%PATH%;./bin/win32i386

set CP=.;./resources;./conf
for %%i in ("lib\*.jar") do call catenv.bat %%i
for %%i in ("plugins\*.jar") do call catenv.bat %%i

java -classpath %CP% -Dtaverna.scrollDesktop -ea org.embl.ebi.escience.scuflui.workbench.Workbench