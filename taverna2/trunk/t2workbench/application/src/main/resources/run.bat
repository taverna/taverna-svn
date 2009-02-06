@ECHO OFF

REM Taverna startup script

REM go to the distribution directory
pushd "%~dp0"


REM 300 MB memory
set ARGS="-Xmx300m"

REM Internal system properties
set ARGS=%ARGS% -Draven.profile=file:conf/current-profile.xml
set ARGS=%ARGS% -Djava.system.class.loader=net.sf.taverna.raven.prelauncher.BootstrapClassLoader 
set ARGS=%ARGS% -Dsun.swing.enableImprovedDragGesture 
REM set ARGS=%ARGS% "-Dtaverna.startup=%~dp0"
set ARGS=%ARGS% "-Dtaverna.startup=."


REM proxy settings - uncomment and change as neccessary

REM set ARGS=%ARGS% -Dhttp.proxyHost=192.168.1.1
REM set ARGS=%ARGS% -Dhttp.proxyPort=8080
REM set ARGS=%ARGS% -Dhttp.proxyUser=fred
REM set ARGS=%ARGS% -Dhttp.proxyPassword=s3cret
REM set ARGS=%ARGS% -Dhttp.nonProxyHosts = localhost|127.0.0.1|*.mydomain.com

java %ARGS% -jar lib\prelauncher-1.10-SNAPSHOT.jar

REM restore current directory
popd
