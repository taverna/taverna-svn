@ECHO OFF
java -Draven.profile=file:conf/current-profile.xml -Djava.system.class.loader=net.sf.taverna.raven.prelauncher.BootstrapClassLoader -jar lib/prelauncher-1.7-SNAPSHOT.jar