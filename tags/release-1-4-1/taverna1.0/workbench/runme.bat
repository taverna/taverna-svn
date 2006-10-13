@echo off

set OMII_CLIENT_HOME=@OMII_CLIENT_HOME@

set TAVERNA_OMII_CONF=%OMII_CLIENT_HOME%\conf

set TAVERNA_OMII_JARS=%OMII_CLIENT_HOME%\lib\bouncycastle-jce-jdk13-119.jar
set TAVERNA_OMII_JARS=%TAVERNA_OMII_JARS%;%OMII_CLIENT_HOME%\lib\castor-0.9.7.jar
set TAVERNA_OMII_JARS=%TAVERNA_OMII_JARS%;%OMII_CLIENT_HOME%\lib\itinnov-grid-utils-omii1.jar
set TAVERNA_OMII_JARS=%TAVERNA_OMII_JARS%;%OMII_CLIENT_HOME%\lib\opensaml-1.0.1.jar
set TAVERNA_OMII_JARS=%TAVERNA_OMII_JARS%;%OMII_CLIENT_HOME%\lib\wss4j-itinnov-4.jar
set TAVERNA_OMII_JARS=%TAVERNA_OMII_JARS%;%OMII_CLIENT_HOME%\lib\xmlsec-1.2.1.jar

set TAVERNA_OMII_FILES=%TAVERNA_OMII_CONF%;%TAVERNA_OMII_JARS%

set TAVERNA_OMII_OPTS=-Daxis.ClientConfigFile="%OMII_CLIENT_HOME%\conf\default-client-config.wsdd"
set TAVERNA_OMII_OPTS=%TAVERNA_OMII_OPTS% -Djava.endorsed.dirs="%OMII_CLIENT_HOME%\endorsed"
set TAVERNA_OMII_OPTS=%TAVERNA_OMII_OPTS% -Dtaverna.path="%TAVERNA_OMII_FILES%"

set OPTS=-Xms256m -Xmx512m
set OPTS=%OPTS% -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client

java %OPTS% %TAVERNA_OMII_OPTS% -jar "%~dp0\taverna-launcher-1.4.1.jar" %*
